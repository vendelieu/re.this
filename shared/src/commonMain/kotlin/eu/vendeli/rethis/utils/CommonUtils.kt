@file:JvmName("SharedCommonUtilsKt")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readDecimalCrlf
import kotlin.jvm.JvmName
import kotlinx.io.Buffer

fun Buffer.parseCode(default: RespCode) =
    if (this == EMPTY_BUFFER) default else RespCode.fromCode(readByte())

private const val NEGATIVE_SIGN: Byte = '-'.code.toByte()

/**
 * Called right after an aggregate type byte was read: consumes a negative size header
 * (the RESP2 null array `*-1\r\n`) and returns `true`, or leaves the buffer untouched and returns `false`.
 */
fun Buffer.skipNullAggregateHeader(): Boolean {
    if (this == EMPTY_BUFFER || size == 0L || this[0] != NEGATIVE_SIGN) return false
    readDecimalCrlf()
    return true
}
