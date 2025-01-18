package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.core.RespCode.entries

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
        private val EntryMap = entries.associate { it.code to it }
        fun fromCode(code: Byte): RespCode = EntryMap[code] ?: exception {
            "No suitable message type found - ${code.toInt().toChar()}"
        }

        fun isSimpleType(type: RespCode): Boolean = type != ARRAY && type != SET && type != PUSH && type != MAP
    }
}
