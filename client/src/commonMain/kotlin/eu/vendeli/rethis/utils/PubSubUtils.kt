package eu.vendeli.rethis.utils

import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.core.SubscriptionManager
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.Push
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.utils.readCompleteResponseInto
import eu.vendeli.rethis.types.common.PubSubEvent
import eu.vendeli.rethis.types.common.PubSubEventParser
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.common.encode
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi

@ReThisInternal
@OptIn(InternalIoApi::class, InternalAPI::class)
suspend fun SubscriptionManager.registerSubscription(
    target: SubscribeTarget,
    handler: PubSubHandler,
    provider: ConnectionProvider? = null,
) = client.run {
    val providerResolved = provider ?: client.topology.route(target.encode(cfg.charset))
    val connection = providerResolved.borrowConnection()
    val ctx = currentCoroutineContext() + CoroutineName("pubsub-handler#$target") + Job(rootJob)

    scope.launch(CoLocalConn(connection, false) + ctx) {
        val conn = currentCoroutineContext()[CoLocalConn]!!.connection
        val job = currentCoroutineContext()[Job]!!

        subscriptions.registerSubscription(target, providerResolved, handler, job)

        try {
            conn.doRequest(target.encode(cfg.charset).data)

            while (isActive) {
                val payload = Buffer()
                conn.input.readCompleteResponseInto(payload)
                val push = when (val frame = RTypeDecoder.decode(payload, cfg.charset)) {
                    is Push -> frame
                    is RArray -> Push(frame.value)
                    else -> null
                } ?: continue
                val event = PubSubEventParser.parse(push) ?: continue

                when (event) {
                    is PubSubEvent.Message -> {
                        subscriptions.globalHandlers.forEach {
                            runCatching { it.onMessage(event.kind, event.channel, event.payload, event.pattern) }
                                .onFailure { e -> logger.warn("Global handler threw in onMessage", e) }
                        }
                        runCatching {
                            handler.onMessage(event.kind, event.channel, event.payload, event.pattern)
                        }.onFailure {
                            logger.warn("Handler threw in onMessage", it)
                        }
                    }

                    is PubSubEvent.Subscribed -> {
                        subscriptions.globalHandlers.forEach {
                            runCatching { it.onSubscribe(event.kind, event.target, event.activeCount) }
                                .onFailure { e -> logger.warn("Global handler threw in onSubscribe", e) }
                        }
                        runCatching {
                            handler.onSubscribe(event.kind, event.target, event.activeCount)
                        }.onFailure {
                            logger.warn("Handler threw in onSubscribe", it)
                        }
                    }

                    is PubSubEvent.Unsubscribed -> {
                        subscriptions.globalHandlers.forEach {
                            runCatching { it.onUnsubscribe(event.kind, event.target, event.activeCount) }
                                .onFailure { e -> logger.warn("Global handler threw in onUnsubscribe", e) }
                        }
                        runCatching {
                            handler.onUnsubscribe(event.kind, event.target, event.activeCount)
                        }.onFailure {
                            logger.warn("Handler threw in onUnsubscribe", it)
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Caught exception in $target channel handler", e)
            runCatching { handler.onException(target, e) }
            subscriptions.globalHandlers.forEach { runCatching { it.onException(target, e) } }
        } finally {
            subscriptions.unregisterHandler(target, handler, job)
            connectionFactory.dispose(conn)
        }
    }
}
