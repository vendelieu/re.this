package eu.vendeli.rethis.api.processor.utils

/**
 * Converts a Redis token string to its corresponding RedisToken property name (valid Kotlin identifier).
 */
internal fun tokenToRedisTokenPropertyName(token: String): String = when (token) {
    "" -> "EMPTY"
    "*" -> "ASTERISK"
    "=" -> "EQUALS"
    "~" -> "TILDE"
    "$" -> "DOLLAR"
    else -> token
        .uppercase()
        .replace("-", "_")
        .replace(" ", "_")
}
