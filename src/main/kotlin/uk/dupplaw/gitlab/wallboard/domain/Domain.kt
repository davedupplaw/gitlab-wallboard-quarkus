package uk.dupplaw.gitlab.wallboard.domain

import io.quarkus.runtime.annotations.RegisterForReflection

interface AllowedOverWebsocket

@RegisterForReflection
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
