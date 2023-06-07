package uk.dupplaw.gitlab.wallboard.config

import io.smallrye.config.ConfigMapping
import org.eclipse.microprofile.config.inject.ConfigProperty
import jakarta.enterprise.context.ApplicationScoped

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
        val projectMapping: SonarqubeProjectMapping
)

@ConfigMapping(prefix = "quality.service.sonarqube.map", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
fun interface SonarqubeProjectMapping {
    fun map(): Map<String, Long>
}
