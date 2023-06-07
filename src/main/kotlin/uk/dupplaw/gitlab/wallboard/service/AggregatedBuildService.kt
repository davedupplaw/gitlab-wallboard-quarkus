package uk.dupplaw.gitlab.wallboard.service

import jakarta.enterprise.context.ApplicationScoped
import uk.dupplaw.gitlab.wallboard.config.BuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.BuildService
import uk.dupplaw.gitlab.wallboard.domain.Project
import uk.dupplaw.gitlab.wallboard.service.gitlab.GitLabCIBuildService

@ApplicationScoped
class AggregatedBuildService(
        private val buildServiceConfiguration: BuildServiceConfiguration,
        private val gitlabCIBuildService: GitLabCIBuildService
) : BuildService {
    override fun retrieveBuildInformation(project: Project) =
            buildServices()
                    .firstOrNull { dealsWithProject(project) }
                    ?.retrieveBuildInformation(project)

    // TODO: This should utilise a mapping in the properties, or use a default
    @Suppress("UNUSED_PARAMETER")
    private fun dealsWithProject(ignored: Project) = true

    private fun buildServices() = buildServiceConfiguration.names.map { makeService(it) }

    private fun makeService(name: String) = when (name) {
        "gitlab-ci" -> gitlabCIBuildService
        else -> throw IllegalArgumentException("Unknown build service $name")
    }
}
