package uk.dupplaw.gitlab.wallboard.service.gitlab

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.config.GitLabCIBuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.Build
import uk.dupplaw.gitlab.wallboard.domain.BuildService
import uk.dupplaw.gitlab.wallboard.domain.BuildStatus
import uk.dupplaw.gitlab.wallboard.domain.Project
import java.net.URL
import javax.enterprise.context.ApplicationScoped
import kotlin.random.Random

@ApplicationScoped
class GitLabCIBuildService(
    val gitLabCIBuildServiceConfiguration: GitLabCIBuildServiceConfiguration
) : BuildService {
    private val logger = KotlinLogging.logger {}

    private val baseUrl = "/api/v4"
    private val pipelinesUrl =
        "$baseUrl/projects/:projectId/pipelines?simple=true&per_page=1&order_by=updated_at&sort=desc&ref=:ref"
    private val pipelineUrl = "$baseUrl/projects/:projectId/pipelines/:pipelineId?simple=true"
    private val jobUrl = "$baseUrl/projects/:projectId/pipelines/:pipelineId/jobs?per_page=1&scope=:scope"

    override fun retrieveBuildInformation(project: Project) = flow {
        while (true) {
            getLatestBuild(project.id)?.let {
                emit(getBuildInfo(project.id, it))
            }

            val delayTimeMillis = Random.nextLong(
                gitLabCIBuildServiceConfiguration.minRefreshTime,
                gitLabCIBuildServiceConfiguration.maxRefreshTime
            )
            logger.trace { "Waiting $delayTimeMillis ms before updating builds ${project.name}" }
            delay(delayTimeMillis)
        }
    }

    private fun getLatestBuild(projectId: Long): Long? {
        val projectUrl = "https://${gitLabCIBuildServiceConfiguration.host}$pipelinesUrl"
            .replace(":projectId", projectId.toString())
            .replace(
                ":ref",
                gitLabCIBuildServiceConfiguration.overriddenRefs.refs()[projectId]
                    ?: gitLabCIBuildServiceConfiguration.ref
            )

        logger.trace { "Getting latest build at $projectUrl" }
        return URL(projectUrl).openConnection().apply {
            readTimeout = 800
            connectTimeout = 200
            setRequestProperty("Private-Token", gitLabCIBuildServiceConfiguration.token)
        }.getInputStream().use { ins ->
            ObjectMapper().readTree(ins).firstOrNull()?.get("id")?.asLong()
        }
    }

    private fun getBuildInfo(projectId: Long, buildId: Long): Build {
        val buildUrl = "https://${gitLabCIBuildServiceConfiguration.host}$pipelineUrl"
            .replace(":projectId", projectId.toString())
            .replace(":pipelineId", buildId.toString())

        logger.trace { "Getting build info: $buildUrl" }
        return URL(buildUrl).openConnection().apply {
            readTimeout = 800
            connectTimeout = 200
            setRequestProperty("Private-Token", gitLabCIBuildServiceConfiguration.token)
        }.getInputStream().use { ins ->
            ObjectMapper().readTree(ins).let { node ->
                val id = node.get("id").asLong()
                val actualStatus = node.get("status").asText()
                val status = when (actualStatus) {
                    "success" -> BuildStatus.SUCCESS
                    "failed" -> BuildStatus.FAIL
                    "canceled", "skipped" -> BuildStatus.WARNING
                    "created", "waiting_for_resources", "preparing", "pending", "running" -> BuildStatus.RUNNING
                    else -> BuildStatus.UNKNOWN
                }
                val lastBuild = node.get("updated_at").asText()
                val webUrl = node.get("web_url").asText()
                val user = node.get("user").get("name").asText()
                val scope = when (status) {
                    BuildStatus.FAIL, BuildStatus.WARNING, BuildStatus.RUNNING -> actualStatus
                    else -> null
                }

                Build(
                    id = id,
                    projectId = projectId,
                    buildUrl = webUrl,
                    status = status,
                    lastBuildTimestamp = lastBuild,
                    user = user,
                    failReason = actualStatus,
                    currentStatusReason = scope?.let { getJobInfo(projectId, id, it) } ?: ""
                )
            }
        }
    }

    fun getJobInfo(projectId: Long, pipelineId: Long, scope: String): String {
        try {
            val jobUrl = "https://${gitLabCIBuildServiceConfiguration.host}$jobUrl"
                .replace(":projectId", projectId.toString())
                .replace(":pipelineId", pipelineId.toString())
                .replace(":scope", scope)

            logger.info { "Getting job info: $jobUrl" }

            return URL(jobUrl).openConnection().apply {
                readTimeout = 800
                connectTimeout = 200
                setRequestProperty("Private-Token", gitLabCIBuildServiceConfiguration.token)
            }.getInputStream().use { ins ->
                ObjectMapper().readTree(ins).get(0).get("name").asText()
            }
        } catch (e: Exception) {
            logger.warn { "Error getting job information" }
            logger.warn { e }
            return ""
        }
    }
}
