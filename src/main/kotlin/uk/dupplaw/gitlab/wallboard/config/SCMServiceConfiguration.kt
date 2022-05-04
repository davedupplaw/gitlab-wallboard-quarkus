package uk.dupplaw.gitlab.wallboard.config

import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
data class SCMServiceConfiguration(
    @ConfigProperty(name = "scm.services") val names: List<String>,
    val gitLabServiceConfiguration: GitLabServiceConfiguration
)

@ApplicationScoped
data class GitLabServiceConfiguration(
    @ConfigProperty(name = "scm.service.gitlab.host") val host: String,
    @ConfigProperty(name = "scm.service.gitlab.token") val token: String,
    @ConfigProperty(name = "scm.service.gitlab.whitelists.groups") val groupsWhitelist: List<Long>,
    @ConfigProperty(name = "scm.service.gitlab.blacklist.projects") val projectBlacklist: Optional<List<Long>>
)

@ApplicationScoped
data class BuildServiceConfiguration(
    @ConfigProperty(name = "build.services") val names: List<String>,
    val gitlabCIBuildServiceConfiguration: GitLabCIBuildServiceConfiguration
)

@ApplicationScoped
data class GitLabCIBuildServiceConfiguration(
    @ConfigProperty(name = "build.service.gitlab-ci.host") val host: String,
    @ConfigProperty(name = "build.service.gitlab-ci.token") val token: String,
)