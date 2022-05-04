package uk.dupplaw.gitlab.wallboard.domain

import kotlinx.coroutines.flow.Flow

interface SCMService {
    fun retrieveProjects() : Flow<Project>
}

interface BuildService {
    fun retrieveBuildInformation(project: Project): Flow<Build>
}
