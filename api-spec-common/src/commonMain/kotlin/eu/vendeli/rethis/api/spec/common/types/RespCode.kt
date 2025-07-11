package eu.vendeli.rethis.api.spec.common.types

enum class RespCode(
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
    ATTRIBUTE('`'.code.toByte()),
    SET('~'.code.toByte()),
    PUSH('>'.code.toByte()),
    ;

    override fun toString(): String = code.toInt().toChar().toString()

    companion object {
        private val EntryMap = entries.associateBy { it.code }

        fun fromCode(code: Byte): RespCode = EntryMap[code] ?: throw IllegalArgumentException(
            "No suitable message type found - ${code.toInt().toChar()}",
        )
    }
}
