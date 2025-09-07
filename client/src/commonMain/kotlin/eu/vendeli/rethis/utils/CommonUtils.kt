@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.shared.response.common.HostAndPort
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.types.common.Address
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.io.Buffer

internal val COMMON_LOGGER = KtorSimpleLogger("eu.vendeli.rethis.ReThisCommonLogger")

expect val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher

expect fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T

internal fun Buffer.parseCode(default: RespCode) =
    if (this == EMPTY_BUFFER) default else RespCode.fromCode(readByte())

internal suspend inline fun <T> withRetry(
    cfg: ReThisConfiguration,
    block: suspend (attempt: Int) -> T,
): T {
    val logger = cfg.loggerFactory.get("eu.vendeli.rethis.ReThis")
    var currentDelay = cfg.retry.initialDelay.inWholeMilliseconds
    var ex: Exception? = null
    repeat(cfg.retry.times - 1) {
        val attempt = it + 1
        logger.trace { "Attempt $attempt of ${cfg.retry.times}" }
        try {
            return block(it)
        } catch (e: Exception) {
            logger.debug("Caught exception at $attempt attempt", e)
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
