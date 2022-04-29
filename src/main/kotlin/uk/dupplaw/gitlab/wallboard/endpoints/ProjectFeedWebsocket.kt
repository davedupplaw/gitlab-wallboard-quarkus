package uk.dupplaw.gitlab.wallboard.endpoints

import uk.dupplaw.gitlab.wallboard.domain.ProjectInfoMessage
import uk.dupplaw.gitlab.wallboard.serialization.JsonEncoder
import javax.enterprise.context.ApplicationScoped
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint(value = "/api/ws/project-feed", encoders = [JsonEncoder::class])
@ApplicationScoped
class ProjectFeedWebsocket {
    @OnOpen
    fun onOpen(session: Session) {
        println("onOpen>")

        val dummyProjects = (1..10).map {
            ProjectInfoMessage(it.toString(), "Project $it")
        }

        dummyProjects.forEach { session.asyncRemote?.sendObject(it) }
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
