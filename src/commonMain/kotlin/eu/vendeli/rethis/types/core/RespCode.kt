package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.core.RespCode.entries

internal enum class RespCode(
    val code: Byte,
    val type: Type,
) {
    SIMPLE_STRING('+'.code.toByte(), Type.SIMPLE),
    SIMPLE_ERROR('-'.code.toByte(), Type.SIMPLE),
    INTEGER(':'.code.toByte(), Type.SIMPLE),
    BULK('$'.code.toByte(), Type.SIMPLE_AGG),
    ARRAY('*'.code.toByte(), Type.AGGREGATE),
    NULL('_'.code.toByte(), Type.SIMPLE),
    BOOLEAN('#'.code.toByte(), Type.SIMPLE),
    DOUBLE(','.code.toByte(), Type.SIMPLE),
    BIG_NUMBER('('.code.toByte(), Type.SIMPLE),
    BULK_ERROR('!'.code.toByte(), Type.SIMPLE_AGG),
    VERBATIM_STRING('='.code.toByte(), Type.SIMPLE_AGG),
    MAP('%'.code.toByte(), Type.AGGREGATE),
    ATTRIBUTE('`'.code.toByte(), Type.AGGREGATE),
    SET('~'.code.toByte(), Type.AGGREGATE),
    PUSH('>'.code.toByte(), Type.AGGREGATE),
    ;

    val isSimple = type == Type.SIMPLE || type == Type.SIMPLE_AGG

    enum class Type {
        SIMPLE, SIMPLE_AGG, AGGREGATE
    }

    companion object {
        private val EntryMap = entries.associate { it.code to it }
        fun fromCode(code: Byte): RespCode = EntryMap[code] ?: exception {
            "No suitable message type found - ${code.toInt().toChar()}"
        }
    }
}
