package eu.vendeli.rethis.core

import eu.vendeli.rethis.types.interfaces.LoggerFactory
import io.ktor.util.collections.*
import io.ktor.util.logging.*

object DefaultLoggerFactory : LoggerFactory {
    private val loggerCache = ConcurrentMap<String, Logger>()

    override fun get(name: String): Logger = loggerCache.getOrPut(name) { KtorSimpleLogger(name) }
}
