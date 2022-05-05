package uk.dupplaw.gitlab.wallboard.service

import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.config.BuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.BuildService
import uk.dupplaw.gitlab.wallboard.domain.Project
import uk.dupplaw.gitlab.wallboard.service.gitlab.GitLabCIBuildService
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class AggregatedBuildService(
    private val buildServiceConfiguration: BuildServiceConfiguration,
    private val gitlabCIBuildService: GitLabCIBuildService
) : BuildService {
    private val logger = KotlinLogging.logger {}

    override fun retrieveBuildInformation(project: Project) = flow {
        try {
            logger.info { "Getting builds for $project" }
            buildServices()
                .firstOrNull { dealsWithProject(project) }
                ?.retrieveBuildInformation(project)
                ?.collect {
                    emit(it)
                }
        } catch (e: Exception) {
            logger.error {
                "Caught exception getting build information. This stops the build information being " +
                        "retrieved for this project until the next time the project is read from the server."
            }
            logger.error { e }
        }
    }

    // TODO: This should utilise a mapping in the properties, or use a default
    private fun dealsWithProject(project: Project) = true

    private fun buildServices() = buildServiceConfiguration.names.map { makeService(it) }

    private fun makeService(name: String) = when (name) {
        "gitlab-ci" -> gitlabCIBuildService
        else -> throw IllegalArgumentException("Unknown build service $name")
    }
}
