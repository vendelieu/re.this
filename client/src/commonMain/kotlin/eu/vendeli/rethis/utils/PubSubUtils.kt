package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.decoders.pubsub.SubEventDecoder
import eu.vendeli.rethis.types.common.Subscription
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import io.ktor.utils.io.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// todo handle sentinel mode separately
internal suspend inline fun ReThis.registerSubscription(
    target: String,
    subscription: Subscription,
) {
    subscriptions.registerSubscription(target, subscription)
    if (subscriptions.subscriptionJobs[target] != null) return

    val request = subscription.type.request(target)
    val provider = topology.route(request)
    val connection = provider.borrowConnection()
    val handlerJob = scope.launch(CoLocalConn(connection)) {
        val conn = currentCoroutineContext()[CoLocalConn]!!.connection
        try {
            conn.doRequest(request.buffer)

            while (isActive) {
                conn.input.awaitContent()
                val event = SubEventDecoder.decode(conn.input.readBuffer(), cfg.charset)
                logger.debug("Handling event in $target channel subscription")

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

    subscriptions.registerHandler(target, handlerJob)
}
