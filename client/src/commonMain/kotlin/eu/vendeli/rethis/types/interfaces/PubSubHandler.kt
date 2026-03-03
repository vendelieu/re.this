package eu.vendeli.rethis.types.interfaces

import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.types.common.PubSubKind
import eu.vendeli.rethis.types.common.SubscribeTarget

interface PubSubHandler {
    suspend fun onSubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long)
    suspend fun onUnsubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long)
    suspend fun onMessage(kind: PubSubKind, channel: String, message: RType, pattern: String? = null)
    suspend fun onException(target: SubscribeTarget, ex: Exception)
}
