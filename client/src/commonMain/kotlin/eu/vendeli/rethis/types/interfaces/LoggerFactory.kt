package eu.vendeli.rethis.types.interfaces

import io.ktor.util.logging.Logger

fun interface LoggerFactory {
    fun get(name: String): Logger
}
