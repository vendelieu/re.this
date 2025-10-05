package eu.vendeli.rethis.types.interfaces

import io.ktor.util.logging.*

fun interface LoggerFactory {
    fun get(name: String): Logger
}
