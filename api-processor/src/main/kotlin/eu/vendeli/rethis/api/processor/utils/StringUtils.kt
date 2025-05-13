package eu.vendeli.rethis.api.processor.utils

fun String.toCamelCase(): String {
    val regex = Regex(
        "[^\\p{Alnum}]+|" +
            "(?<=\\p{Lower})(?=\\p{Upper})|" +
            "(?<=\\p{Upper})(?=\\p{Upper}\\p{Lower})|" +
            "(?<=\\p{Alpha})(?=\\d)|" +
            "(?<=\\d)(?=\\p{Alpha})",
    )
    val words = this.split(regex).filter { it.isNotEmpty() }
    if (words.isEmpty()) return ""
    return words.first().lowercase() +
        words.drop(1).joinToString("") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

fun String.toPascalCase(): String {
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


internal fun String.checkMatch(type: String) =
    matches(Regex("^(?!.*-.*\\[$type]).*\\[$type].*$", RegexOption.IGNORE_CASE))
