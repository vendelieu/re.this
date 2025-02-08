package eu.vendeli.rethis.utils

import io.ktor.utils.io.core.*

internal object Const {
    const val DEFAULT_HOST = "127.0.0.1"
    const val DEFAULT_PORT = 6379

    val EOL = "\r\n".toByteArray()

    val TRUE_BYTE = 't'.code.toByte()
    val FALSE_BYTE = 'f'.code.toByte()

    val NEWLINE_BYTE = '\n'.code.toByte()
    val CARRIAGE_RETURN_BYTE = '\r'.code.toByte()

}

const val REDIS_JSON_ROOT_PATH = "$"
