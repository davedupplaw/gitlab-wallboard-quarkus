package uk.dupplaw.gitlab.wallboard.domain

import kotlinx.coroutines.flow.Flow

/**
 * Service that provides information about the commits, such as who made the commits,
 * when the last commit was done, etc.
 */
fun interface SCMService {
    fun retrieveProjects() : Flow<Project>
}

/**
 * Service that provides information about the current build status of a pipeline.
 * Provides information such as whether it passed or failed, what the reason was for
 * any failure, etc.
 */
fun interface BuildService {
    fun retrieveBuildInformation(project: Project): Build?
}

/**
 * Service that provides specific information about a job associated to the project.
 * This could, for example, be information about whether a certain build step passed or
 * failed, or whether certain deployment passed, or whatever.
 */
fun interface JobService {
    fun retrieveJobs(project: Project): List<JobInfo>
}

/**
 * Service that provides information about the quality metrics of a particular project.
 * Provides information such as code coverage, security vulnerabilities, etc.
 */
interface QualityService {
    fun allProjects(): Flow<Quality>
    fun getQualityMeasures(component: String, branch: String): Quality?
}
