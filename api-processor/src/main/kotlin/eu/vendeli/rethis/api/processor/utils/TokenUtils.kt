package eu.vendeli.rethis.api.processor.utils

/**
 * Converts a Redis token string to its corresponding RedisToken property name (valid Kotlin identifier).
 */
internal fun tokenToRedisTokenPropertyName(token: String): String = when (token) {
    "" -> "EMPTY_STRING"
    "*" -> "ASTERISK"
    "=" -> "EQUALS"
    "~" -> "TILDE"
    "$" -> "DOLLAR"
    "+" -> "PLUS"
    "-" -> "MINUS"
    else -> token
        .uppercase()
        .replace("-", "_")
        .replace(" ", "_")
        .replace(".", "_")
}

// The Redis spec serializes a CLI-quoted empty-string sentinel (e.g. MIGRATE's optional
// empty key-selector) as the literal token `""` — two double-quote characters. On the
// wire that's a zero-length bulk string, which is exactly what an empty Kotlin token
// produces, so treat the two representations as equivalent when matching against the spec.
internal fun specTokenMatches(specToken: String?, kotlinToken: String): Boolean = when {
    specToken == kotlinToken -> true
    specToken == "\"\"" && kotlinToken.isEmpty() -> true
    else -> false
}
