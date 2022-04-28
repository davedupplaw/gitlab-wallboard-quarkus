package uk.dupplaw.gitlab.wallboard.endpoints

import com.fasterxml.jackson.databind.ObjectMapper
import uk.dupplaw.gitlab.wallboard.domain.HelloMessage
import uk.dupplaw.gitlab.wallboard.domain.Message
import javax.enterprise.context.ApplicationScoped
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

class JsonEncoder : Encoder.Text<Message> {
    override fun encode(msg: Message): String = ObjectMapper().writeValueAsString(msg)

    override fun init(p0: EndpointConfig?) {
        // Not required
    }
    override fun destroy() {
        // Not required
    }
}

@ServerEndpoint(value = "/api/ws/project-feed", encoders = [JsonEncoder::class])
@ApplicationScoped
class ProjectFeedWebsocket {
    @OnOpen
    fun onOpen(session: Session) {
        println("onOpenJohnny>")
        session.asyncRemote?.sendObject(
            HelloMessage("Hello Johnny!")
        ) ?: println("Didn't send initial message")
    }

    @OnClose
    fun onClose(session: Session) {
        println("onClose>")
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        println("onError>: $throwable")
    }

    @OnMessage
    fun onMessage(message: String) {
        println("onMessage>: $message")
    }
}
