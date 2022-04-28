package uk.dupplaw.gitlab.wallboard.endpoints

import javax.enterprise.context.ApplicationScoped
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/api/ws/project-feed")
@ApplicationScoped
class ProjectFeedWebsocket {

    @OnOpen
    fun onOpen(session: Session?) {
        println("onOpen>")
    }

    @OnClose
    fun onClose(session: Session?) {
        println("onClose>")
    }

    @OnError
    fun onError(session: Session?, throwable: Throwable) {
        println("onError>: $throwable")
    }

    @OnMessage
    fun onMessage(message: String) {
        println("onMessage>: $message")
    }
}
