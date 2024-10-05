@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

fun RType.isOk() = unwrap<String>() == "OK"

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.cast(): T = this as T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.safeCast(): T? = this as? T

internal suspend inline fun ReThis.registerSubscription(
    regCommand: String,
    unRegCommand: String,
    exHandler: ReThisExceptionHandler? = null,
    target: String,
    messageMarker: String,
    handler: SubscriptionHandler,
) {
    val connection = connectionPool.acquire()
    val handlerJob = coLaunch(CoLocalConn(connection)) {
        val conn = coroutineContext[CoLocalConn]!!.connection
        try {
            conn.output.writeBuffer(bufferValues(listOf(regCommand.toArg(), target.toArg()), cfg.charset))
            conn.output.flush()

            while (isActive) {
                conn.input.awaitContent()
                val msg = conn.input.readRedisMessage()
                val input = if (msg is Push) msg.value else msg.safeCast<RArray>()?.value
                logger.debug("Handling event in $target channel subscription")

                input
                    ?.takeIf {
                        it.first().value == messageMarker && it.getOrNull(1)?.value == target
                    }?.also {
                        handler.onMessage(this@registerSubscription, input.last().unwrap<String>() ?: "")
                    }

                delay(1)
            }
        } catch (e: Exception) {
            logger.debug("Caught exception in $target channel handler")
            exHandler?.handle(e)
        } finally {
            conn.output.writeBuffer(bufferValues(listOf(unRegCommand.toArg(), target.toArg()), cfg.charset))
            conn.output.flush()
            connectionPool.release(conn)
        }
    }
    subscriptionHandlers[target] = handlerJob
}
