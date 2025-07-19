package eu.vendeli.rethis.api.spec.common.types

open class ClusterException(override val message: String? = null, override val cause: Throwable? = null) :
    ReThisException()

class KeyAbsentException(override val message: String, override val cause: Throwable? = null) : ClusterException()

class CrossSlotOperationException(override val message: String, override val cause: Throwable? = null) :
    ClusterException()

class RedirectUnstableException(
    override val message: String,
    val origin: String,
    override val cause: Throwable? = null,
) :
    ClusterException()

class RedirectAskException(
    override val message: String, val slot: Int,
    val host: String,
    val port: Int, override val cause: Throwable? = null,
) : ClusterException()

class RedirectMovedException(
    override val message: String,
    val slot: Int,
    val host: String,
    val port: Int,
    override val cause: Throwable? = null,
) : ClusterException()

class DownUnboundSlotException(
    override val message: String,
    val origin: String,
    override val cause: Throwable? = null,
) : ClusterException()

class DownReadOnlyStateException(
    override val message: String,
    val origin: String,
    override val cause: Throwable? = null,
) : ClusterException()
