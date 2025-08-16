package eu.vendeli.rethis.api.spec.common.types

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

inline fun processingException(cause: Throwable? = null, message: () -> String? = { null }): Nothing =
    throw DataProcessingException(message(), cause)
