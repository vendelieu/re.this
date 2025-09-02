package eu.vendeli.rethis.types.common

import io.ktor.util.logging.*

fun interface LoggerFactory {
    fun get(name: String): Logger
}
