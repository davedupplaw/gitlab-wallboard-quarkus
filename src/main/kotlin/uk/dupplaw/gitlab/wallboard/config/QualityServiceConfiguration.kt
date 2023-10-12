package uk.dupplaw.gitlab.wallboard.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@ApplicationScoped
data class QualityServiceConfiguration(
    @ConfigProperty(name = "quality.services") val names: List<String>,
    val sonarqubeQualityServiceConfiguration: SonarqubeQualityServiceConfiguration
)

@ApplicationScoped
data class SonarqubeQualityServiceConfiguration(
    @ConfigProperty(name = "quality.service.sonarqube.host") val host: String,
    @ConfigProperty(name = "quality.service.sonarqube.token") val token: String,
    @ConfigProperty(name = "quality.service.sonarqube.projects") val projects: List<String>,
    @ConfigProperty(name = "quality.service.sonarqube.projectMapping") val projectMapping: Map<String, Long>,
    @ConfigProperty(name = "quality.service.sonarqube.branches") val branches: Optional<Map<String, String>>
)
