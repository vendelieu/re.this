package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.interfaces.MessageBufferEventHandler
import eu.vendeli.rethis.types.interfaces.MessageEventHandler
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import eu.vendeli.rethis.types.interfaces.toPubSubHandler
import eu.vendeli.rethis.utils.registerSubscription

public suspend fun ReThis.subscribe(vararg channel: String, callback: PubSubHandler) {
    channel.forEach { registerSubscription(SubscribeTarget.Channel(it), callback) }
}

public suspend fun ReThis.subscribe(vararg channel: String, callback: MessageEventHandler) {
    channel.forEach { registerSubscription(SubscribeTarget.Channel(it), callback.toPubSubHandler(this)) }
}

public suspend fun ReThis.subscribe(vararg channel: String, callback: MessageBufferEventHandler) {
    channel.forEach { registerSubscription(SubscribeTarget.Channel(it), callback.toPubSubHandler(this)) }
}
