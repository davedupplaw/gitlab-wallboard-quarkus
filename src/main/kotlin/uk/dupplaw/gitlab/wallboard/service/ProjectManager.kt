package uk.dupplaw.gitlab.wallboard.service

import io.quarkus.runtime.StartupEvent
import io.quarkus.vertx.ConsumeEvent
import io.vertx.mutiny.core.eventbus.EventBus
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes

@ApplicationScoped
class ProjectManager(
    private val eventBus: EventBus,
    private val scmService: AggregatedSCMService,
    private val buildService: AggregatedBuildService,
    private val projectCache: ProjectCache,
) {
    private val logger = KotlinLogging.logger {}

    @ConsumeEvent("new-session")
    @Suppress("unused")
    fun newSessionOpened(sessionId: String) = runBlocking {
        logger.info { "Project cache: ${projectCache.projects()}" }

        projectCache.projects().forEach { eventBus.publish("project", it) }
        projectCache.builds().forEach { eventBus.publish("build", it) }
    }

    fun updateOnStartup(@Observes event: StartupEvent) {
        logger.info { "Startup event: Getting latest projects" }
        updateProjects()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun updateProjects() = runBlocking {
        try {
            val projects = scmService.retrieveProjects()
                .onEach { projectCache[it.id] = it }
                .onEach { eventBus.publish("project", it) }
                .toList()

            logger.info { "Projects: $projects" }

            // Create a flow for each project's builds
            projects.forEach {
                GlobalScope.launch {
                    buildService.retrieveBuildInformation(it).collect { info ->
                        eventBus.publish("build", info)
                        projectCache[it.id] = info
                    }
                }
            }
        } catch (e: Throwable) {
            logger.warn { "An exception occurred that was caught during the retrieval of build information" }
            logger.info { "Catching the exception allows the retrieval loop to continue." }
            logger.warn { e }
        }
    }
}
