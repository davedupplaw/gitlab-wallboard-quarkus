package uk.dupplaw.gitlab.wallboard.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
data class SystemConfiguration(
    @ConfigProperty(name = "system.dashboard.name") val name: String,
    @ConfigProperty(name = "system.version") val version: String,
    @ConfigProperty(name = "system.build") val build: String,
    val toggles: Toggles
)

@ApplicationScoped
data class Toggles(
    @ConfigProperty(name = "toggles.sortOrder") val sortOrder: String
)
