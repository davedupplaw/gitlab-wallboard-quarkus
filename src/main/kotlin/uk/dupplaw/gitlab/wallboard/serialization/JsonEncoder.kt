package uk.dupplaw.gitlab.wallboard.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import uk.dupplaw.gitlab.wallboard.domain.AllowedOverWebsocket
import jakarta.websocket.Encoder
import jakarta.websocket.EndpointConfig

class JsonEncoder : Encoder.Text<AllowedOverWebsocket> {
    override fun encode(msg: AllowedOverWebsocket): String = ObjectMapper().writeValueAsString(msg)

    override fun init(p0: EndpointConfig?) {
        // Not required
    }
    override fun destroy() {
        // Not required
    }
}
