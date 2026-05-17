package eu.vendeli.rethis.api.processor.utils

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
