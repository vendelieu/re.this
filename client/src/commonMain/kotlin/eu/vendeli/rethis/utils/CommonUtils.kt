@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.api.spec.common.response.common.HostAndPort
import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.configuration.RetryConfiguration
import eu.vendeli.rethis.types.common.Address
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

expect val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher

expect fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.safeCast(): T? = this as? T

internal suspend inline fun <T> withRetry(
    cfg: RetryConfiguration,
    block: suspend () -> T,
): T {
    var currentDelay = cfg.initialDelay.inWholeMilliseconds
    var ex: Exception? = null
    repeat(cfg.times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            if (ex == null) {
                ex = e
            } else {
                ex.addSuppressed(e)
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * cfg.factor).toLong().coerceAtMost(cfg.maxDelay.inWholeMilliseconds)
    }
    if (ex != null) throw ex
    return block()
}

internal inline fun HostAndPort.toAddress(): Address = Address(host, port)
internal inline fun Address.toHostAndPort(): HostAndPort? =
    if (socket is InetSocketAddress) HostAndPort(socket.hostname, socket.port) else null

@OptIn(ExperimentalContracts::class)
inline fun requireOrPanic(condition: Boolean, message: () -> String) {
    contract {
        returns() implies condition
    }
    if (!condition) {
        val message = message()
        throw ReThisException(message)
    }
}

inline fun panic(message: String): Nothing = throw ReThisException(message)
