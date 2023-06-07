package uk.dupplaw.gitlab.wallboard.domain

import io.quarkus.runtime.annotations.RegisterForReflection

interface AllowedOverWebsocket

@RegisterForReflection
data class Project(
        val id: Long,
        val name: String,
        val projectUrl: String,
        var quality: Quality? = null
) : AllowedOverWebsocket

enum class BuildStatus {
    SUCCESS,
    FAIL,
    WARNING,
    UNKNOWN,
    RUNNING,
}

@RegisterForReflection
data class Build(
        val id: Long,
        val projectId: Long,
        val buildUrl: String,
        val status: BuildStatus,
        val lastBuildTimestamp: String,
        val user: String,
        val textStatus: String? = null,
        val currentStatusReasons: List<String> = listOf()
) : AllowedOverWebsocket

interface Quality {
    val projectId: Long
    val type: String
}

@RegisterForReflection
data class SonarQuality(
        override val projectId: Long,
        val component: String,
        val url: String,
        val securityRating: Int? = null,
        val reliabilityRating: Int? = null,
        val coverage: Double? = null,
        val duplications: Double? = null
) : AllowedOverWebsocket, Quality {
    override val type: String = "sonar"
}
