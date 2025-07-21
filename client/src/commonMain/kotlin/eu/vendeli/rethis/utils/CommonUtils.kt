@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.api.spec.common.response.common.HostAndPort
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.types.common.Address
import io.ktor.network.sockets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.io.Buffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

expect val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher

expect fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T

@ReThisInternal
suspend fun ReThis.execute(request: CommandRequest): Buffer = topology.route(request).execute(request)

internal suspend inline fun <T> withRetry(
    cfg: ReThisConfiguration,
    block: suspend (attempt: Int) -> T,
): T {
    var currentDelay = cfg.retry.initialDelay.inWholeMilliseconds
    var ex: Exception? = null
    repeat(cfg.retry.times - 1) {
        cfg.loggerFactory.get("eu.vendeli.rethis.ReThis").debug("Attempt ${it + 1} of ${cfg.retry.times}")
        try {
            return block(it)
        } catch (e: Exception) {
            if (ex == null) {
                ex = e
            } else {
                ex.addSuppressed(e)
            }
        }
        delay(currentDelay)
        currentDelay = (currentDelay * cfg.retry.factor).toLong().coerceAtMost(cfg.retry.maxDelay.inWholeMilliseconds)
    }
    if (ex != null) throw ex
    return block(cfg.retry.times)
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
