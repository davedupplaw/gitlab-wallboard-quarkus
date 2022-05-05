package uk.dupplaw.gitlab.wallboard.endpoints

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@QuarkusTest
class SystemInfoControllerTest {
    @Test
    fun testVersionEndpoint() {
        val json = given().get("/api/system/version").jsonPath()
        val version = json.get<String>("version")
        val build = json.get<String>("buildNumber")

        assertThat(version).isEqualTo("1.0.0")
        assertThat(build).isEqualTo("v000000")
    }
}
