package eu.vendeli.rethis.types.interfaces

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.utils.StringCodec
import eu.vendeli.rethis.types.common.PubSubKind
import eu.vendeli.rethis.types.common.SubscribeTarget
import kotlinx.io.Buffer

sealed interface MessageHandler

fun interface MessageEventHandler : MessageHandler {
    suspend fun onMessage(client: ReThis, message: String)
}

fun interface MessageBufferEventHandler : MessageHandler {
    suspend fun onMessage(client: ReThis, message: Buffer)
}

fun MessageHandler.toPubSubHandler(client: ReThis) = object : PubSubHandler {
    override suspend fun onSubscribe(
        kind: PubSubKind,
        target: SubscribeTarget,
        subscribedChannels: Long,
    ) {
    }

    override suspend fun onUnsubscribe(
        kind: PubSubKind,
        target: SubscribeTarget,
        subscribedChannels: Long,
    ) {
    }

    override suspend fun onMessage(
        kind: PubSubKind,
        channel: String,
        message: RType,
        pattern: String?,
    ) {
        val incomingMessage = (message as? BulkString)?.value ?: return
        when (this@toPubSubHandler) {
            is MessageBufferEventHandler -> {
                onMessage(client, incomingMessage)
            }

            is MessageEventHandler -> {
                onMessage(
                    client,
                    StringCodec.decodeToString(
                        client.cfg.charset,
                        incomingMessage,
                    ),
                )
            }
        }
    }

    override suspend fun onException(target: SubscribeTarget, ex: Exception) {}
}
