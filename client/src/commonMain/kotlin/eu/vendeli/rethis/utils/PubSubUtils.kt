package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.decoders.pubsub.SubEventDecoder
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.Subscription
import eu.vendeli.rethis.types.common.SubscriptionWorker
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal suspend fun ReThis.registerSubscription(
    target: String,
    subscription: Subscription,
    provider: ConnectionProvider? = null,
) {
    val request = subscription.type.request(target)
    val provider = provider ?: topology.route(request)

    subscriptions.registerSubscription(target, subscription)
    if (subscriptions.isHandlerRegistered(target, provider)) return

    val connection = provider.borrowConnection()
    val handlerJob = scope.launch(CoLocalConn(connection, false)) {
        val conn = currentCoroutineContext()[CoLocalConn]!!.connection
        try {
            conn.doRequest(request.buffer)

            while (isActive) {
                conn.input.awaitContent()
                val event = SubEventDecoder.decode(conn.input.readBuffer(), cfg.charset)
                logger.debug { "Handling event in $target channel subscription" }

                val inputType = event.first()
                when (inputType) {
                    subscription.type.regMarker -> {
                        val targetCh = event[1]
                        subscriptions.eventHandler?.onSubscribe(targetCh, event.last().toLong())
                    }

                    subscription.type.unRegMarker -> {
                        val targetCh = event[1]
                        subscriptions.eventHandler?.onUnsubscribe(targetCh, event.last().toLong())
                        subscriptions.unsubscribe(targetCh)
                    }

                    subscription.type.messageMarker if event[1] == target -> {
                        subscription.handler.onMessage(this@registerSubscription, event.last())
                    }
                }

                delay(1)
            }
        } catch (e: Exception) {
            logger.error("Caught exception in $target channel handler")
            subscriptions.eventHandler?.onException(target, e)
        } finally {
            subscriptions.unregisterHandler(target, subscription)
            if (subscriptions.subscriptionsHandlers[target]?.isEmpty() == true) provider.releaseConnection(connection)
            subscriptions.unsubscribe(target)
        }
    }

    subscriptions.registerHandler(target, SubscriptionWorker(provider, handlerJob))
}
