package uk.dupplaw.gitlab.wallboard.service.sonarqube

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import io.github.oshai.kotlinlogging.KotlinLogging
import uk.dupplaw.gitlab.wallboard.config.SonarqubeQualityServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.QualityService
import uk.dupplaw.gitlab.wallboard.domain.SonarQuality
import java.net.URL
import java.util.*

@ApplicationScoped
class SonarqubeQualityService(
        val config: SonarqubeQualityServiceConfiguration
) : QualityService {
    private val logger = KotlinLogging.logger {}
    private val metrics = listOf("security_review_rating", "reliability_rating", "coverage", "duplicated_lines_density")

    override fun allProjects() = flow {
        while (true) {
            logger.info { "Using project mapping: ${config.projectMapping}" }
            config.projects.asSequence().forEach { componentName ->
                try {
                    getQualityMeasures(componentName.trim('\"'))?.let { emit(it) }
                } catch (e: Exception) {
                    logger.warn(e) { "Caught error getting quality information for $componentName" }
                }
            }
            delay(10_000L)
        }
    }

    override fun getQualityMeasures(component: String) =
            "https://${config.host}/api/measures/component?component=$component&metricKeys=${metrics.joinToString(",")}".let { apiUrl ->
                URL(apiUrl).openConnection().apply {
                    readTimeout = 800
                    connectTimeout = 200
                    setRequestProperty("Authorization", "Basic ${String(Base64.getEncoder().encode("${config.token}:".toByteArray()))}")
                }.getInputStream().use { ins ->
                    ObjectMapper().readTree(ins)?.let { json ->
                        json["component"]["measures"]?.let { measures ->
                            SonarQuality(
                                    config.projectMapping[component.sanitize()] ?: -1,
                                    component,
                                    "https://${config.host}/dashboard?id=$component",
                                    measures.find { it["metric"]?.asText() == "security_review_rating" }?.get("value")?.asInt(),
                                    measures.find { it["metric"]?.asText() == "reliability_rating" }?.get("value")?.asInt(),
                                    measures.find { it["metric"]?.asText() == "coverage" }?.get("value")?.asDouble(),
                                    measures.find { it["metric"]?.asText() == "duplicated_lines_density" }?.get("value")?.asDouble(),
                            )
                        }
                    }
                }
            }

    private fun String.sanitize() = this.replace(Regex("[.:]"), "-")
}

fun main() = runBlocking {
    SonarqubeQualityService(SonarqubeQualityServiceConfiguration(
            "https://sonarqube.mocca.yunextraffic.cloud",
            "squ_9320b9efa232d6a8695348f3c00a6cf1c57e3bce",
            listOf(
                    "com.siemens.mobility.fs:network-state",
                    "com.siemens.mobility.fs:cycle-plan-service",
                    "com.siemens.mobility.fs:decision-engine-service",
                    "com.siemens.mobility.fs:utc-config-service",
                    "com.siemens.mobility.fs:network-config-service",
                    "com.siemens.mobility.fs:fs-ui",
            ),
            mapOf()
    )).allProjects().collect { println(it) }
}
