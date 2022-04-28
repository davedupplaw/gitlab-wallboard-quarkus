package uk.dupplaw.gitlab.wallboard.endpoints

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class VersionControllerTest {
    @Test
    fun testVersionEndpoint() {
        given()
          .`when`().get("/api/version")
          .then()
             .statusCode(200)
             .body(`is`("1.0.0"))
    }
}
