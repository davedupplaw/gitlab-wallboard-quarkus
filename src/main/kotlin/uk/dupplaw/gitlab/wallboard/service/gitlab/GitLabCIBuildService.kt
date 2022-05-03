package uk.dupplaw.gitlab.wallboard.service.gitlab

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.config.GitLabCIBuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.Build
import uk.dupplaw.gitlab.wallboard.domain.BuildService
import uk.dupplaw.gitlab.wallboard.domain.BuildStatus
import uk.dupplaw.gitlab.wallboard.domain.Project
import java.net.URL
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class GitLabCIBuildService(
    val gitLabCIBuildServiceConfiguration: GitLabCIBuildServiceConfiguration
) : BuildService {
    private val logger = KotlinLogging.logger {}

    private val baseUrl = "/api/v4"
    private val pipelinesUrl = "$baseUrl/projects/:projectId/pipelines?simple=true&per_page=1&order_by=updated_at&sort=desc&ref=master"
    private val pipelineUrl = "$baseUrl/projects/:projectId/pipelines/:pipelineId?simple=true"

    override fun retrieveBuildInformation(project: Project) =
        getLatestBuild(project.id)?.let {
            getBuildInfo(project.id, it)
        }

    private fun getLatestBuild(projectId: Long): Long? {
        val projectUrl = "https://${gitLabCIBuildServiceConfiguration.host}$pipelinesUrl"
            .replace(":projectId", projectId.toString())

        logger.info { "Getting latest build at $projectUrl" }
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

        logger.info { "Getting build info: $buildUrl" }
        return URL(buildUrl).openConnection().apply {
            readTimeout = 800
            connectTimeout = 200
            setRequestProperty("Private-Token", gitLabCIBuildServiceConfiguration.token)
        }.getInputStream().use { ins ->
            ObjectMapper().readTree(ins).let { node ->
                val id = node.get("id").asLong()
                val status = when (node.get("status").asText()) {
                    "success" -> BuildStatus.SUCCESS
                    "failed" -> BuildStatus.FAIL
                    "canceled", "skipped" -> BuildStatus.WARNING
                    "created", "waiting_for_resources", "preparing", "pending", "running" -> BuildStatus.RUNNING
                    else -> BuildStatus.UNKNOWN
                }
                val lastBuild = node.get("updated_at").asText()
                val webUrl = node.get("web_url").asText()

                Build(id, projectId, webUrl, status, lastBuild)
            }
        }
    }
}
