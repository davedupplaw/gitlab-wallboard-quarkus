package uk.dupplaw.gitlab.wallboard.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import uk.dupplaw.gitlab.wallboard.domain.Message
import javax.websocket.Encoder
import javax.websocket.EndpointConfig

class JsonEncoder : Encoder.Text<Message> {
    override fun encode(msg: Message): String = ObjectMapper().writeValueAsString(msg)

    override fun init(p0: EndpointConfig?) {
        // Not required
    }
    override fun destroy() {
        // Not required
    }
}
