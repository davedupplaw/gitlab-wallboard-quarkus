package uk.dupplaw.gitlab.wallboard.endpoints

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.quarkus.vertx.ConsumeEvent
import io.vertx.mutiny.core.eventbus.EventBus
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.domain.Build
import uk.dupplaw.gitlab.wallboard.domain.Project
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class DomainToMessageConverter(
    private val eventBus: EventBus
) {
    private val logger = KotlinLogging.logger {}

    @ConsumeEvent("project")
    fun projectInfo(project: Project) {
        logger.trace { "Converting project to message: $project" }
        val node = ObjectMapper().valueToTree<ObjectNode>(project)
        node.put("type", "project-info")
        eventBus.publish("message", node.toString())
    }

    @ConsumeEvent("build")
    fun buildInfo(build: Build) {
        logger.trace { "Converting build to message: $build" }

        val node = ObjectMapper().valueToTree<ObjectNode>(build)
        node.put("type", "build-info")
        eventBus.publish("message", node.toString())
    }
}
