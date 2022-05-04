package uk.dupplaw.gitlab.wallboard.domain

interface AllowedOverWebsocket

data class Project(
    val id: Long,
    val name: String,
    val projectUrl: String,
) : AllowedOverWebsocket

enum class BuildStatus {
    SUCCESS,
    FAIL,
    WARNING,
    UNKNOWN,
    RUNNING,
}

data class Build(
    val id: Long,
    val projectId: Long,
    val buildUrl: String,
    val status: BuildStatus,
    val lastBuildTimestamp: String,
    val user: String,
    val failReason: String? = null,
    val failedJob: String? = null
) : AllowedOverWebsocket
