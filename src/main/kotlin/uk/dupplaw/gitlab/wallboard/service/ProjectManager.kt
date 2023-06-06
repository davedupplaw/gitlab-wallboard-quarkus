package uk.dupplaw.gitlab.wallboard.service

import io.quarkus.runtime.StartupEvent
import io.quarkus.vertx.ConsumeEvent
import io.vertx.mutiny.core.eventbus.EventBus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.domain.QualityService
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import kotlin.random.Random

@ApplicationScoped
class ProjectManager(
        private val eventBus: EventBus,
        private val scmService: AggregatedSCMService,
        private val buildService: AggregatedBuildService,
        private val qualityService: QualityService,
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

    fun updateProjects() = runBlocking {
        GlobalScope.launch {
            delay(10_000L)
            qualityService.allProjects()
                    .map { projectCache[it.projectId]?.apply { this.quality = it } }
                    .filterNotNull()
                    .onEach { logger.info { "New quality stats for project ${it.name}: ${it.quality}" } }
                    .collect { eventBus.publish("project", it) }
        }

        GlobalScope.launch {
            scmService.retrieveProjects()
                    .onEach { projectCache[it.id] = it }
                    .onEach { eventBus.publish("project", it) }
                    .collect { logger.info { "Retrieved project info $it" } }
        }

        GlobalScope.launch {
            delay(5000)
            var amount = 100.0
            while (true) {
                projectCache.projects().forEachIndexed { i, project ->
                    launch {
                        delay(i * Random.nextDouble(amount).toLong())
                        buildService.retrieveBuildInformation(project)?.let { build ->
                            logger.info { "Retrieved project build info $build" }
                            projectCache[project.id] = build
                            eventBus.publish("build", build)
                        }
                    }
                }
                amount = 10_000.0
                delay(projectCache.projects().size * 250L)
            }
        }
    }
}
