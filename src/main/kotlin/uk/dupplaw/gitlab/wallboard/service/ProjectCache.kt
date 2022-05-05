package uk.dupplaw.gitlab.wallboard.service

import uk.dupplaw.gitlab.wallboard.domain.Build
import uk.dupplaw.gitlab.wallboard.domain.Project
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProjectCache {
    private val cache: MutableMap<Long, Project> = ConcurrentHashMap()
    private val buildCache: MutableMap<Long, Build> = ConcurrentHashMap()

    operator fun set(id: Long, project: Project) {
        cache[id] = project
    }
    operator fun set(id: Long, build: Build) {
        buildCache[id] = build
    }

    operator fun get(id: Long) = cache[id]

    fun projects() = cache.values
    fun builds() = buildCache.values
}
