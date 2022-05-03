package uk.dupplaw.gitlab.wallboard.service.gitlab

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.config.GitLabServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.Project
import uk.dupplaw.gitlab.wallboard.domain.SCMService
import java.net.URL
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class GitLabService(
    private val gitLabServiceConfiguration: GitLabServiceConfiguration
) : SCMService {
    private val logger = KotlinLogging.logger {}

    private val baseUrl = "/api/v4"
    private val projectsUrl = "$baseUrl/projects?simple=true&per_page=10"
    private val groupProjectsUrl = "$baseUrl/groups/:groupId/projects?simple=true&per_page=10"

    init {
        logger.info { "Using GitLab SCM Service" }
    }

    override fun retrieveProjects() = flow {
        when {
            gitLabServiceConfiguration.groupsWhitelist.isNotEmpty() -> {
                logger.info { "A group whitelist exists, so only projects from these groups: ${gitLabServiceConfiguration.groupsWhitelist}" }
                getProjectsInGroups()
            }
            else -> getAllProjects()
        }
    }

    private suspend fun FlowCollector<Project>.getProjectsInGroups() {
        gitLabServiceConfiguration.groupsWhitelist.forEach { groupId ->
            val path = groupProjectsUrl.replace(":groupId", groupId.toString())
            loopOverPagesInResponse(path)
        }
    }

    private suspend fun FlowCollector<Project>.loopOverPagesInResponse(path: String) {
        var page = 1
        var list: List<Project>
        do {
            logger.trace { "At page $page" }
            list = getProjectsWithPage(path, page++)
                .filter { gitLabServiceConfiguration.projectBlacklist.map { x -> x.contains(it.id) }.orElse(true) }
                .onEach { emit(it) }
        } while (list.isNotEmpty())
    }

    private suspend fun FlowCollector<Project>.getAllProjects() {
        loopOverPagesInResponse(projectsUrl)
    }

    private fun getProjectsWithPage(path: String, page: Int): List<Project> {
        val pagedUrl = "https://${gitLabServiceConfiguration.host}$path&page=$page"
        logger.trace { "Contacting $pagedUrl" }
        return URL(pagedUrl).openConnection().apply {
            readTimeout = 800
            connectTimeout = 200
            setRequestProperty("Private-Token", gitLabServiceConfiguration.token)
        }.getInputStream().use { ins ->
            ObjectMapper().readTree(ins).map { node ->
                val id = node.get("id").asLong()
                val name = node.get("name").asText()

                Project(id, name)
            }
        }
    }
}
