package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.Push
import eu.vendeli.rethis.shared.utils.readCompleteResponseInto
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi

@ReThisInternal
@OptIn(InternalIoApi::class, InternalAPI::class)
suspend fun ReThis.registerSubscription(
    target: SubscribeTarget,
    handler: PubSubHandler,
    provider: ConnectionProvider? = null,
) {
    val providerResolved = provider ?: topology.route(target.encode(cfg.charset))
    val connection = providerResolved.borrowConnection()
    val ctx = currentCoroutineContext() + CoroutineName("pubsub-handler#$target") + Job(rootJob)

    scope.launch(CoLocalConn(connection, false) + ctx) {
        val conn = currentCoroutineContext()[CoLocalConn]!!.connection
        val job = currentCoroutineContext()[Job]!!
        val payload = Buffer()

        subscriptions.registerSubscription(target, providerResolved, handler, job)

        try {
            conn.doRequest(target.encode(cfg.charset).buffer)

            while (isActive) {
                conn.input.readCompleteResponseInto(payload)
                val frame = RTypeDecoder.decode(payload, cfg.charset)
                val push = frame as? Push ?: continue
                val event = PubSubEventParser.parse(push) ?: continue

                // Dispatch both global and local handlers
                when (event) {
                    is PubSubEvent.Message -> {
                        subscriptions.globalHandlers.forEach {
                            it.onMessage(event.kind, event.channel, event.payload, event.pattern)
                        }
                        handler.onMessage(event.kind, event.channel, event.payload, event.pattern)
                    }

                    is PubSubEvent.Subscribed -> {
                        subscriptions.globalHandlers.forEach {
                            it.onSubscribe(event.kind, event.target, event.activeCount)
                        }
                        handler.onSubscribe(event.kind, event.target, event.activeCount)
                    }

                    is PubSubEvent.Unsubscribed -> {
                        subscriptions.globalHandlers.forEach {
                            it.onUnsubscribe(event.kind, event.target, event.activeCount)
                        }
                        handler.onUnsubscribe(event.kind, event.target, event.activeCount)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error("Caught exception in $target channel handler", e)
            subscriptions.globalHandlers.forEach { it.onException(target, e) }
        } finally {
            subscriptions.unregisterHandler(target, handler, job)
            providerResolved.releaseConnection(conn)
        }
    }
}
