package eu.vendeli.rethis.api.processor.utils

internal object NameNormalizer {
    /** kebab-case or UPPER-SPACE → camelCase */
    fun normalizeParam(name: String): String =
        name.lowercase()
            .split('-', ' ', '_')
            .mapIndexed { i, part ->
                if (i == 0) part else part.replaceFirstChar { it.uppercase() }
            }.joinToString("")

    /** kebab-case or UPPER-SPACE → PascalCase */
    fun normalizeClass(name: String): String =
        name.split('-', ' ', '_').joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
}
