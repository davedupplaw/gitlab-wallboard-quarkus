package uk.dupplaw.gitlab.wallboard.domain

data class Version(
    val version: String,
    val buildNumber: String
)

data class SystemInfo(
    val name: String,
    val version: Version,
)


