package uk.dupplaw.gitlab.wallboard.endpoints

import uk.dupplaw.gitlab.wallboard.config.SystemConfiguration
import uk.dupplaw.gitlab.wallboard.domain.SystemInfo
import uk.dupplaw.gitlab.wallboard.domain.Version
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/api/system")
class SystemInfoController(
    val systemConfiguration: SystemConfiguration
) {
    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    fun version() = Version(
        systemConfiguration.version,
        systemConfiguration.build
    )

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    fun info() = SystemInfo(
        systemConfiguration.name,
        version(),
    )
}
