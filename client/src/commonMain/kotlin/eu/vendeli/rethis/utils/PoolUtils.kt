package eu.vendeli.rethis.utils

import eu.vendeli.rethis.core.ConnectionPool
import eu.vendeli.rethis.types.common.RConnection
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal suspend inline fun <R> ConnectionPool.withConnection(block: (RConnection) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    var exception: Throwable? = null
    val conn = acquire()
    try {
        return block(conn)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(conn, exception)
    }
}

internal fun ConnectionPool.closeFinally(connection: RConnection, cause: Throwable?): Unit = when {
    cause == null -> release(connection)
    else -> try {
        release(connection)
    } catch (closeException: Throwable) {
        cause.addSuppressed(closeException)
    }
}
