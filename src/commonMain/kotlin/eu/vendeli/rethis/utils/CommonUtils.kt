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

@Suppress("NOTHING_TO_INLINE")
private inline fun String?.isEqTo(other: String) = if (this != null) {
    compareTo(other.lowercase()) == 0
} else {
    false
}

internal suspend inline fun ReThis.registerSubscription(
    regCommand: String,
    unRegCommand: String,
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
                val msg = conn.input.readRedisMessage(cfg.charset)
                val input = if (msg is Push) msg.value else msg.safeCast<RArray>()?.value
                logger.debug("Handling event in $target channel subscription")

                val inputType = input?.firstOrNull()?.value?.safeCast<String>()
                when {
                    inputType.isEqTo(regCommand) -> {
                        val targetCh = input?.getOrNull(1)?.unwrap<String>() ?: target
                        val subscribers = input?.lastOrNull()?.unwrap<Long>() ?: 0L
                        subscriptions.eventHandler?.onSubscribe(targetCh, subscribers)
                    }

                    inputType.isEqTo(unRegCommand) -> {
                        val targetCh = input?.getOrNull(1)?.unwrap<String>() ?: target
                        val subscribers = input?.lastOrNull()?.unwrap<Long>() ?: 0L
                        subscriptions.eventHandler?.onUnsubscribe(targetCh, subscribers)
                        subscriptions.unsubscribe(targetCh)
                    }

                    inputType == messageMarker && input.getOrNull(1)?.value == target -> {
                        handler.onMessage(this@registerSubscription, input.last().unwrap<String>() ?: "")
                    }
                }

                delay(1)
            }
        } catch (e: Exception) {
            logger.error("Caught exception in $target channel handler")
            subscriptions.eventHandler?.onException(target, e)
        } finally {
            conn.output.writeBuffer(bufferValues(listOf(unRegCommand.toArg(), target.toArg()), cfg.charset))
            conn.output.flush()
            connectionPool.release(conn)
            subscriptions.unsubscribe(target)
        }
    }

    subscriptions.jobs[target] = handlerJob
}
