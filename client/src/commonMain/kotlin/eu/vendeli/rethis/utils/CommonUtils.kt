@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.shared.response.common.HostAndPort
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*

internal val COMMON_LOGGER = KtorSimpleLogger("eu.vendeli.rethis.ReThisCommonLogger")

expect val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher

expect fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T

internal suspend inline fun <T> withRetry(
    cfg: ReThisConfiguration,
    block: suspend (attempt: Int) -> T,
): T {
    val logger = cfg.loggerFactory.get("eu.vendeli.rethis.ReThis")
    var currentDelay = cfg.retry.initialDelay.inWholeMilliseconds
    var ex: Exception? = null
    val totalAttempts = cfg.retry.times.coerceAtLeast(1)
    for (attempt in 0 until totalAttempts) {
        logger.trace { "Attempt ${attempt + 1} of $totalAttempts" }
        try {
            return block(attempt)
        } catch (e: Exception) {
            logger.debug("Caught exception at ${attempt + 1} attempt", e)
            if (ex == null) {
                ex = e
            } else {
                ex.addSuppressed(e)
            }
        }
        // no backoff after the final attempt, just surface the failure
        if (attempt == totalAttempts - 1) break
        delay(currentDelay)
        currentDelay = (currentDelay * cfg.retry.factor).toLong().coerceAtMost(cfg.retry.maxDelay.inWholeMilliseconds)
    }
    throw ex!!
}

internal inline fun HostAndPort.toAddress(): Address = Address(host, port)
internal inline fun Address.toHostAndPort(): HostAndPort? =
    if (socket is InetSocketAddress) HostAndPort(socket.hostname, socket.port) else null

internal suspend fun isInTx(): Boolean {
    val coLocalCon = currentCoroutineContext()[CoLocalConn]
    return coLocalCon != null && coLocalCon.isTx
}
