package uk.dupplaw.gitlab.wallboard.endpoints

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.dupplaw.gitlab.wallboard.config.SystemConfiguration
import jakarta.inject.Inject

@QuarkusTest
class SystemInfoControllerTest {
    @Inject private lateinit var systemConfiguration: SystemConfiguration

    @Test
    fun testVersionEndpoint() {
        val json = given().get("/api/system/version").jsonPath()
        val version = json.get<String>("version")
        val build = json.get<String>("buildNumber")

        assertThat(version).isEqualTo(systemConfiguration.version)
        assertThat(build).isEqualTo(systemConfiguration.build)
    }
}
