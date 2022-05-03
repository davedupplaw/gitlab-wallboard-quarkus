package uk.dupplaw.gitlab.wallboard.service

import io.quarkus.runtime.StartupEvent
import io.quarkus.vertx.ConsumeEvent
import io.vertx.mutiny.core.eventbus.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.domain.Build
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
    private lateinit var receiveChannel: ReceiveChannel<Build>

    @ConsumeEvent("new-session")
    fun newSessionOpened(sesionId: String) = runBlocking {
        logger.info { "Project cache: ${projectCache.projects()}" }

        projectCache.projects().forEach { eventBus.publish("project", it) }
        projectCache.builds().forEach { eventBus.publish("build", it) }
    }

    fun updateOnStartup(@Observes event: StartupEvent) {
        logger.info { "Startup event: Getting latest projects" }
        runBlocking {
            updateProjects()
        }
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    suspend fun updateProjects() {
        this.receiveChannel = GlobalScope.produce {
            val projects = scmService.retrieveProjects()
                .onEach { projectCache[it.id] = it }
                .onEach { eventBus.publish("project", it) }
                .toList()

            logger.info { "Projects: $projects" }

            while (true) {
                projects
                    .map {
                        launch {
                            buildService.retrieveBuildInformation(it)?.let { info ->
                                eventBus.publish("build", info)
                                projectCache[it.id] = info
                            }
                        }
                    }

                val timeMillis = 10_000L
                logger.info { "Waiting $timeMillis ms before updating builds" }
                delay(timeMillis)
            }
        }
    }
}
