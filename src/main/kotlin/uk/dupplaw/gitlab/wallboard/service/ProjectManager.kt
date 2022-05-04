package uk.dupplaw.gitlab.wallboard.service

import io.quarkus.runtime.StartupEvent
import io.quarkus.vertx.ConsumeEvent
import io.vertx.mutiny.core.eventbus.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import kotlin.random.Random

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

            projects.forEach {
                GlobalScope.launch {
                    while (true) {
                        buildService.retrieveBuildInformation(it)?.let { info ->
                            eventBus.publish("build", info)
                            projectCache[it.id] = info
                        }
                        val timeMillis = Random.nextLong(10_000, 15_000)
                        logger.info { "Waiting $timeMillis ms before updating builds ${it.name}" }
                        delay(timeMillis)
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
