package uk.dupplaw.gitlab.wallboard.service.gitlab

import uk.dupplaw.gitlab.wallboard.config.BuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.config.GitLabCIBuildServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.JobInfo
import uk.dupplaw.gitlab.wallboard.domain.JobService
import uk.dupplaw.gitlab.wallboard.domain.Project

class GitLabCIJobService(
    val buildServiceConfiguration: BuildServiceConfiguration,
    val gitLabCIBuildServiceConfiguration: GitLabCIBuildServiceConfiguration
) : JobService {
    override fun retrieveJobs(project: Project): List<JobInfo> {
        TODO("Not yet implemented")
    }

}
