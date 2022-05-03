package uk.dupplaw.gitlab.wallboard.endpoints

import io.quarkus.vertx.ConsumeEvent
import io.vertx.mutiny.core.eventbus.EventBus
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.serialization.JsonEncoder
import javax.enterprise.context.ApplicationScoped
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint(value = "/api/ws/project-feed", encoders = [JsonEncoder::class])
@ApplicationScoped
class ProjectFeedWebsocket(
    private val eventBus: EventBus,
) {
    private val logger = KotlinLogging.logger {}
    private val openSessions = mutableListOf<Session>()

    @ConsumeEvent("message")
    fun sendMessage(message: String) {
        logger.trace { "Sending message $message" }
        openSessions.forEach { session -> session.asyncRemote?.sendObject(message) }
    }

    @OnOpen
    fun onOpen(session: Session) {
        println("onOpen>")
        openSessions.add(session)
        eventBus.publish("new-session", session.id)
    }

    @OnClose
    fun onClose(session: Session) {
        println("onClose>")
        openSessions.remove(session)
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        println("onError>: $throwable")
        openSessions.remove(session)
    }

    @OnMessage
    fun onMessage(message: String) {
        println("onMessage>: $message")
    }
}
