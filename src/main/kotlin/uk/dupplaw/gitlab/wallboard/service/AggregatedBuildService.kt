package uk.dupplaw.gitlab.wallboard.service

import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.config.BuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.Build
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

    override fun retrieveBuildInformation(project: Project): Build? {
        return try {
            logger.info { "Getting builds for $project" }
            buildServices().first { dealsWithProject(project) }.retrieveBuildInformation(project)
        } catch(e: Exception) {
            logger.error { "Caught exception getting build information" }
            logger.error { e }
            null
        }
    }

    // TODO: This should utilise a mapping in the properties, or use a default
    private fun dealsWithProject(project: Project) = true

    private fun buildServices() = buildServiceConfiguration.names.map { makeService(it) }

    private fun makeService(name: String) = when(name) {
        "gitlab-ci" -> gitlabCIBuildService
        else -> throw IllegalArgumentException("Unknown build service $name")
    }
}
