package uk.dupplaw.gitlab.wallboard.service.gitlab

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.enterprise.context.ApplicationScoped
import uk.dupplaw.gitlab.wallboard.config.GitLabCIBuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.Build
import uk.dupplaw.gitlab.wallboard.domain.BuildService
import uk.dupplaw.gitlab.wallboard.domain.BuildStatus
import uk.dupplaw.gitlab.wallboard.domain.Project
import java.net.URL

private const val PROJECT_ID_TAG = ":projectId"

private const val PRIVATE_TOKEN_HEADER = "Private-Token"

@ApplicationScoped
class GitLabCIBuildService(
    val gitLabCIBuildServiceConfiguration: GitLabCIBuildServiceConfiguration
) : BuildService {
    private val logger = KotlinLogging.logger {}

    private val baseUrl = "/api/v4"
    private val pipelinesUrl =
        "$baseUrl/projects/$PROJECT_ID_TAG/pipelines?simple=true&per_page=1&order_by=id&sort=desc&ref=:ref"
    private val pipelineUrl = "$baseUrl/projects/$PROJECT_ID_TAG/pipelines/:pipelineId?simple=true"
    private val jobUrl = "$baseUrl/projects/$PROJECT_ID_TAG/pipelines/:pipelineId/jobs?per_page=3&scope=:scope"
    private val bridgesUrl = "$baseUrl/projects/$PROJECT_ID_TAG/pipelines/:pipelineId/bridges?per_page=3&scope=:scope"

    override fun retrieveBuildInformation(project: Project) =
        getLatestBuild(project.id)?.let {
            getBuildInfo(project.id, it)
        }

    private fun getLatestBuild(projectId: Long): Long? {
        val projectUrl = "https://${gitLabCIBuildServiceConfiguration.host}$pipelinesUrl"
            .replace(PROJECT_ID_TAG, projectId.toString())
            .replace(
                ":ref",
                gitLabCIBuildServiceConfiguration.overriddenRefs.refs()[projectId]
                    ?: gitLabCIBuildServiceConfiguration.ref
            )

        logger.trace { "Getting latest build at $projectUrl" }
        return URL(projectUrl).openConnection().apply {
            readTimeout = 800
            connectTimeout = 200
            setRequestProperty(PRIVATE_TOKEN_HEADER, gitLabCIBuildServiceConfiguration.token)
        }.getInputStream().use { ins ->
            ObjectMapper().readTree(ins).firstOrNull()?.get("id")?.asLong()
        }
    }

    private fun getBuildInfo(projectId: Long, buildId: Long): Build {
        val buildUrl = "https://${gitLabCIBuildServiceConfiguration.host}$pipelineUrl"
            .replace(PROJECT_ID_TAG, projectId.toString())
            .replace(":pipelineId", buildId.toString())

        logger.trace { "Getting build info: $buildUrl" }
        return URL(buildUrl).openConnection().apply {
            readTimeout = 800
            connectTimeout = 200
            setRequestProperty(PRIVATE_TOKEN_HEADER, gitLabCIBuildServiceConfiguration.token)
        }.getInputStream().use { ins ->
            ObjectMapper().readTree(ins).let { node ->
                val id = node["id"].asLong()
                val actualStatus = node["status"].asText()
                val status = when (actualStatus) {
                    "success" -> BuildStatus.SUCCESS
                    "failed" -> BuildStatus.FAIL
                    "canceled", "skipped" -> BuildStatus.WARNING
                    "created", "waiting_for_resources", "preparing", "pending", "running" -> BuildStatus.RUNNING
                    else -> BuildStatus.UNKNOWN
                }
                val lastBuild = node["updated_at"].asText()
                val webUrl = node["web_url"].asText()
                val user = node["user"]["name"].asText()
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
                    textStatus = actualStatus,
                    currentStatusReasons = (scope?.let {
                        getBridgeInfo(projectId, id, it) + getJobInfo(projectId, id, it)
                    } ?: listOf()).take(3)
                )
            }
        }
    }

    fun getJobInfo(projectId: Long, pipelineId: Long, scope: String): List<String> {
        try {
            val jobUrl = "https://${gitLabCIBuildServiceConfiguration.host}$jobUrl"
                .replace(PROJECT_ID_TAG, projectId.toString())
                .replace(":pipelineId", pipelineId.toString())
                .replace(":scope", scope)

            logger.info { "Getting job info: $jobUrl" }

            return URL(jobUrl).openConnection().apply {
                readTimeout = 800
                connectTimeout = 200
                setRequestProperty(PRIVATE_TOKEN_HEADER, gitLabCIBuildServiceConfiguration.token)
            }.getInputStream().use { ins ->
                ObjectMapper().readTree(ins).map { it["name"].asText() }
            }
        } catch (e: Exception) {
            logger.warn { "Error getting job information" }
            logger.warn { e }
            return listOf()
        }
    }

    fun getBridgeInfo(projectId: Long, pipelineId: Long, scope: String): List<String> {
        try {
            val bridgesUrl = "https://${gitLabCIBuildServiceConfiguration.host}$bridgesUrl"
                .replace(PROJECT_ID_TAG, projectId.toString())
                .replace(":pipelineId", pipelineId.toString())
                .replace(":scope", scope)

            logger.info { "Getting job info: $bridgesUrl" }

            return URL(bridgesUrl).openConnection().apply {
                readTimeout = 800
                connectTimeout = 200
                setRequestProperty(PRIVATE_TOKEN_HEADER, gitLabCIBuildServiceConfiguration.token)
            }.getInputStream().use { ins ->
                ObjectMapper().readTree(ins).map { it["name"].asText() }
            }
        } catch (e: Exception) {
            logger.warn { "Error getting job information" }
            logger.warn { e }
            return listOf()
        }
    }
}
