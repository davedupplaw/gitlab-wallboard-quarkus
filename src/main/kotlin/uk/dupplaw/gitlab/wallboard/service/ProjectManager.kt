package uk.dupplaw.gitlab.wallboard.service

import io.quarkus.runtime.StartupEvent
import io.quarkus.vertx.ConsumeEvent
import io.vertx.mutiny.core.eventbus.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
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

    private val buildJobs = mutableMapOf<Long, Job>()

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
            GlobalScope.launch {
                while(true) {
                    val projects = scmService.retrieveProjects()
                        .map { it to projectCache.has(it.id) }
                        .onEach { (it, _) -> projectCache[it.id] = it }
                        .onEach { (it, _) -> eventBus.publish("project", it) }
                        .onEach { (it, exists) ->
                            if (!exists || buildJobs[it.id]?.isCancelled == true) {
                                val job = GlobalScope.launch {
                                    buildService.retrieveBuildInformation(it).collect { info ->
                                        eventBus.publish("build", info)
                                        projectCache[it.id] = info
                                    }
                                }
                                buildJobs[it.id] = job
                            }
                        }
                        .map { (it, _) -> it }
                        .toList()

                    logger.info { "Projects: $projects" }

                    val timeToWaitUntilProjectsUpdate = 60_000L
                    logger.info { "Waiting $timeToWaitUntilProjectsUpdate ms until next update of projects" }
                    delay(timeToWaitUntilProjectsUpdate)
                }
            }
        } catch (e: Throwable) {
            logger.warn { "An exception occurred that was caught during the retrieval of build information" }
            logger.info { "Catching the exception allows the retrieval loop to continue." }
            logger.warn { e }
        }
    }
}
