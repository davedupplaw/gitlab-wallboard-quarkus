package uk.dupplaw.gitlab.wallboard.config

import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
data class SystemConfiguration(
    @ConfigProperty(name = "system.dashboard.name") val name: String,
    @ConfigProperty(name = "system.version") val version: String,
    @ConfigProperty(name = "system.build") val build: String
)
