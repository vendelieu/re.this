package eu.vendeli.rethis.types.core

/**
 * Enum representing the source of a connection.
 *
 * @property POOL A connection from the connection pool.
 * @property STANDALONE A standalone connection.
 */
enum class ConnectionSource {
    POOL,
    STANDALONE,
}
