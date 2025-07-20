package eu.vendeli.rethis.types.common

import io.ktor.util.logging.Logger

fun interface LoggerFactory {
    fun get(name: String): Logger
}
