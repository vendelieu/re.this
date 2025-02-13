package eu.vendeli.rethis.utils

import io.ktor.utils.io.core.*

internal object Const {
    const val DEFAULT_HOST: String = "127.0.0.1"
    const val DEFAULT_PORT: Int = 6379

    val EOL: ByteArray = "\r\n".toByteArray()

    val TRUE_BYTE: Byte = 't'.code.toByte()
    val FALSE_BYTE: Byte = 'f'.code.toByte()


    val NEWLINE_BYTE = '\n'.code.toByte()
    val CARRIAGE_RETURN_BYTE = '\r'.code.toByte()
}

const val REDIS_JSON_ROOT_PATH = "$"
