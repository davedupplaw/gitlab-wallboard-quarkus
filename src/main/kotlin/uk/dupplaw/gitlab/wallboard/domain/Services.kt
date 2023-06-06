package uk.dupplaw.gitlab.wallboard.domain

import kotlinx.coroutines.flow.Flow

fun interface SCMService {
    fun retrieveProjects() : Flow<Project>
}

fun interface BuildService {
    fun retrieveBuildInformation(project: Project): Build?
}

interface QualityService {
    fun allProjects(): Flow<Quality>
    fun getQualityMeasures(component: String): Quality?
}
