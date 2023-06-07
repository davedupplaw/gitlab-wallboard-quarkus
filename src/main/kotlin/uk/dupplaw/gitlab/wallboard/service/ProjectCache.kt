package uk.dupplaw.gitlab.wallboard.service

import uk.dupplaw.gitlab.wallboard.domain.Build
import uk.dupplaw.gitlab.wallboard.domain.Project
import uk.dupplaw.gitlab.wallboard.domain.Quality
import java.util.concurrent.ConcurrentHashMap
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProjectCache {
    private val cache: MutableMap<Long, Project> = ConcurrentHashMap()
    private val buildCache: MutableMap<Long, Build> = ConcurrentHashMap()
    private val qualityCache: MutableMap<Long, Quality> = ConcurrentHashMap()

    operator fun set(id: Long, project: Project) {
        cache[id] = project
    }

    operator fun set(id: Long, build: Build) {
        buildCache[id] = build
    }

    operator fun set(id: Long, quality: Quality) {
        qualityCache[id] = quality
    }

    operator fun get(id: Long) = cache[id]

    fun has(id: Long) = cache.containsKey(id)
    fun projects() = cache.values
    fun builds() = buildCache.values
    fun quality() = qualityCache.values
}
