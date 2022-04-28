package uk.dupplaw.gitlab.wallboard.endpoints

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/api/version")
class VersionController {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun version() = "1.0.0"
}
