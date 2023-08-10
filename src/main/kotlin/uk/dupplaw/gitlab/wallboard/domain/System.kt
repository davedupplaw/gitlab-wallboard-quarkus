package uk.dupplaw.gitlab.wallboard.domain

import uk.dupplaw.gitlab.wallboard.config.Toggles

data class Version(
    val version: String,
    val buildNumber: String
)

data class SystemInfo(
    val name: String,
    val version: Version,
    val toggles: Toggles
)


