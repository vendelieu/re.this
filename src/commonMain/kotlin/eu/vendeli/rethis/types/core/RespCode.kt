package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.exception

internal enum class RespCode(
    val code: Byte,
) {
    SIMPLE_STRING('+'.code.toByte()),
    SIMPLE_ERROR('-'.code.toByte()),
    INTEGER(':'.code.toByte()),
    BULK('$'.code.toByte()),
    ARRAY('*'.code.toByte()),
    NULL('_'.code.toByte()),
    BOOLEAN('#'.code.toByte()),
    DOUBLE(','.code.toByte()),
    BIG_NUMBER('('.code.toByte()),
    BULK_ERROR('!'.code.toByte()),
    VERBATIM_STRING('='.code.toByte()),
    MAP('%'.code.toByte()),
    SET('~'.code.toByte()),
    PUSH('>'.code.toByte()),
    ;

    companion object {
        fun fromCode(code: Byte): RespCode =
            entries.find { it.code == code } ?: exception { "No suitable message type found" }
    }
}
