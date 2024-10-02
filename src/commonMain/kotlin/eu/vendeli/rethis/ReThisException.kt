package eu.vendeli.rethis

class ReThisException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException()

internal inline fun exception(cause: Throwable? = null, message: () -> String? = { null }): Nothing =
    throw ReThisException(message(), cause)
