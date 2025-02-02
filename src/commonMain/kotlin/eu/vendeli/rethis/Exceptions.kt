package eu.vendeli.rethis

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

class InvalidStateException(
    override val message: String? = null,
) : ReThisException()

internal inline fun processingException(cause: Throwable? = null, message: () -> String? = { null }): Nothing =
    throw DataProcessingException(message(), cause)
