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

private val canonicalSplit = Regex(
    "[^\\p{Alnum}]+|" +
        "(?<=\\p{Lower})(?=\\p{Upper})|" +
        "(?<=\\p{Upper})(?=\\p{Upper}\\p{Lower})|" +
        "(?<=\\p{Alpha})(?=\\d)|" +
        "(?<=\\d)(?=\\p{Alpha})",
)

/**
 * Splits on any non-alphanumeric separator AND on camelCase boundaries, then lowercases each part.
 * Lets Kotlin parameter names match RSpec field names regardless of original casing
 * (`startSlot` ↔ `start-slot` ↔ `start_slot` ↔ `STARTSLOT` all collapse to `startslot`).
 */
internal fun String.canonicalKey(): String =
    split(canonicalSplit)
        .filter { it.isNotEmpty() }
        .joinToString("") { it.lowercase() }
