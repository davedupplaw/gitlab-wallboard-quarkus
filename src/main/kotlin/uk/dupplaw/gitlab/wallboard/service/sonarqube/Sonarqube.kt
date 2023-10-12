package uk.dupplaw.gitlab.wallboard.service.sonarqube

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import uk.dupplaw.gitlab.wallboard.config.SonarqubeQualityServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.QualityService
import uk.dupplaw.gitlab.wallboard.domain.SonarQuality
import java.net.URL
import java.util.*
import kotlin.jvm.optionals.getOrNull

@ApplicationScoped
class SonarqubeQualityService(
    val config: SonarqubeQualityServiceConfiguration
) : QualityService {
    private val logger = KotlinLogging.logger {}
    private val metrics = listOf(
        "security_review_rating",
        "reliability_rating",
        "coverage",
        "duplicated_lines_density",
        "sqale_rating",
        "bugs",
        "code_smells"
    )

    override fun allProjects() = flow {
        while (true) {
            logger.info { "Using project mapping: ${config.projectMapping}" }
            config.projects.asSequence().forEach { componentName ->
                try {
                    getQualityMeasures(
                        componentName.trim('\"'),
                        config.branches.getOrNull()?.get(componentName)?.sanitize() ?: "master"
                    )?.let { emit(it) }
                } catch (e: Exception) {
                    logger.warn(e) { "Caught error getting quality information for $componentName" }
                }
            }
            delay(10_000L)
        }
    }

    override fun getQualityMeasures(component: String, branch: String) =
        ("https://${config.host}/api/measures/component?component=$component" +
                "&branch=$branch" +
                "&metricKeys=${metrics.joinToString(",")}").let { apiUrl ->
            URL(apiUrl).openConnection().apply {
                readTimeout = 800
                connectTimeout = 200
                setRequestProperty(
                    "Authorization",
                    "Basic ${String(Base64.getEncoder().encode("${config.token}:".toByteArray()))}"
                )
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
