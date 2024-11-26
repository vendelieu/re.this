package eu.vendeli.rethis.utils

import io.ktor.utils.io.core.*

internal object Const {
    const val DEFAULT_HOST = "127.0.0.1"
    const val DEFAULT_PORT = 6379

    val EOL = "\r\n".toByteArray()
}
