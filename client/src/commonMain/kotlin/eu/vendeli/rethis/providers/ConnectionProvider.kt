package eu.vendeli.rethis.providers

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import kotlinx.io.Buffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

abstract class ConnectionProvider {
    abstract val node: Address

    abstract suspend fun execute(request: CommandRequest): Buffer

    abstract fun close()

    abstract suspend fun borrowConnection(): RConnection
    abstract suspend fun releaseConnection(conn: RConnection)
    abstract fun hasSpareConnection(): Boolean

    override fun equals(other: Any?): Boolean = node == (other as? ConnectionProvider)?.node
    override fun hashCode(): Int = node.hashCode()
}


@OptIn(ExperimentalContracts::class)
internal suspend inline fun <R> ConnectionProvider.withConnection(block: (RConnection) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    val conn = borrowConnection()
    try {
        return block(conn)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(conn, exception)
    }
}

private suspend fun ConnectionProvider.closeFinally(connection: RConnection, cause: Throwable?): Unit = when {
    cause == null -> releaseConnection(connection)
    else -> try {
        releaseConnection(connection)
    } catch (closeException: Throwable) {
        cause.addSuppressed(closeException)
    }
}
