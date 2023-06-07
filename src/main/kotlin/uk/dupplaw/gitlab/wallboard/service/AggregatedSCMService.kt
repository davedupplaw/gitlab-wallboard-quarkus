package uk.dupplaw.gitlab.wallboard.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import uk.dupplaw.gitlab.wallboard.config.SCMServiceConfiguration
import uk.dupplaw.gitlab.wallboard.domain.SCMService
import uk.dupplaw.gitlab.wallboard.service.gitlab.GitLabService
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class AggregatedSCMService(
        private val scmServiceConfiguration: SCMServiceConfiguration,
        private val gitLabService: GitLabService
) : SCMService {
    private val logger = KotlinLogging.logger {}

    init {
        logger.info { "Using aggregated SCM service" }
    }

    override fun retrieveProjects() = flow {
        while(true) {
            logger.info { "Getting projects..." }

            scmServices().forEach { service ->
                service.retrieveProjects().collect {
                    emit(it)
                }
            }

            val timeToWaitUntilProjectsUpdate = 60_000L
            delay(timeToWaitUntilProjectsUpdate)
        }
    }

    fun scmServices(): List<SCMService> = scmServiceConfiguration.names.map { makeService(it) }

    private fun makeService(scmServiceName: String) =
            when (scmServiceName) {
                "gitlab" -> gitLabService
                else -> throw IllegalArgumentException("SCM service $scmServiceName is not valid")
            }
}
