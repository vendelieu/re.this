package eu.vendeli.rethis.shared.types

open class ReThisException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException()

class ResponseParsingException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : ReThisException()

class RedisError(
    override val message: String? = null,
    val isBulk: Boolean = false,
) : ReThisException()

class DataProcessingException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : ReThisException()

class TransactionInvalidStateException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : ReThisException()

class UnexpectedResponseType(
    override val message: String? = null,
    override val cause: Throwable? = null
) : ReThisException()

/**
 * Exception thrown when the lock is lost in Redis due to TTL expiry or token mismatch.
 */
class LockLostException(override val message: String) : ReThisException()


inline fun processingException(cause: Throwable? = null, message: () -> String? = { null }): Nothing =
    throw DataProcessingException(message(), cause)
