package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.spec.common.types.RespCode

internal fun String.toPascalCase(): String {
    val regex = Regex(
        "[^\\p{Alnum}]+|" +
            "(?<=\\p{Lower})(?=\\p{Upper})|" +
            "(?<=\\p{Upper})(?=\\p{Upper}\\p{Lower})|" +
            "(?<=\\p{Alpha})(?=\\d)|" +
            "(?<=\\d)(?=\\p{Alpha})",
    )
    val words = this.split(regex).filter { it.isNotEmpty() }
    if (words.isEmpty()) return ""
    return words.joinToString("") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

/** kebab-case or UPPER-SPACE → camelCase */
internal fun String.normalizeParam(): String =
    lowercase()
        .split('-', ' ', '_')
        .mapIndexed { i, part ->
            if (i == 0) part else part.replaceFirstChar { it.uppercase() }
        }.joinToString("")

internal fun String.inferResponseType(): RespCode? = when {
    checkMatch("simple string reply") -> RespCode.SIMPLE_STRING
    checkMatch("integer reply") -> RespCode.INTEGER
    checkMatch("boolean reply") -> RespCode.BOOLEAN
    checkMatch("double reply") -> RespCode.DOUBLE
    checkMatch("verbatim string reply") -> RespCode.VERBATIM_STRING
    checkMatch("big number reply") -> RespCode.BIG_NUMBER
    checkMatch("bulk string reply") -> RespCode.BULK
    checkMatch("simple error reply") -> RespCode.SIMPLE_ERROR

    contains("array reply", true) -> RespCode.ARRAY
    contains("set reply", true) -> RespCode.SET
    contains("map reply", true) -> RespCode.MAP
    contains("null reply", true) || contains("nil reply", true) -> RespCode.NULL
    else -> null
}

internal fun String.checkMatch(type: String) =
    matches(Regex("^(?!.*-.*\\[$type]).*\\[$type].*$", RegexOption.IGNORE_CASE))
