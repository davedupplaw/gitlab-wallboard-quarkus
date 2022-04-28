package uk.dupplaw.gitlab.wallboard.endpoints

import uk.dupplaw.gitlab.wallboard.domain.Version
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/api/version")
class VersionController {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun version() = Version("1.0.0")
}
