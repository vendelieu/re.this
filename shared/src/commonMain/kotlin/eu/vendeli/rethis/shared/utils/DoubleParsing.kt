package eu.vendeli.rethis.shared.utils

import eu.vendeli.rethis.shared.types.ResponseParsingException

/** Parses a RESP double, accepting the `inf`, `+inf`, `-inf` and `nan` spellings (case-insensitive). */
internal fun String.toRespDoubleOrNull(): Double? = when {
    equals("inf", ignoreCase = true) || equals("+inf", ignoreCase = true) -> Double.POSITIVE_INFINITY
    equals("-inf", ignoreCase = true) -> Double.NEGATIVE_INFINITY
    equals("nan", ignoreCase = true) -> Double.NaN
    else -> toDoubleOrNull()
}

/**
 * Parses a RESP double (see [toRespDoubleOrNull]).
 *
 * Public so generated command codecs can call it; not intended for direct use.
 *
 * @throws ResponseParsingException if the text is not a valid double.
 */
fun String.toRespDouble(): Double = toRespDoubleOrNull()
    ?: throw ResponseParsingException("Invalid response structure, expected double, given $this")
