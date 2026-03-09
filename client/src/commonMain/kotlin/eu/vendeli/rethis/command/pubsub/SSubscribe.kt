@file:OptIn(ExperimentalJsFileName::class)
@file:JsFileName("_SSubscribe.kt")

package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.interfaces.MessageBufferEventHandler
import eu.vendeli.rethis.types.interfaces.MessageEventHandler
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import eu.vendeli.rethis.types.interfaces.toPubSubHandler
import eu.vendeli.rethis.utils.registerSubscription
import kotlin.js.ExperimentalJsFileName
import kotlin.js.JsFileName

public suspend fun ReThis.sSubscribe(vararg shardChannel: String, callback: PubSubHandler) {
    shardChannel.forEach { subscriptions.registerSubscription(SubscribeTarget.Shard(it), callback) }
}

public suspend fun ReThis.sSubscribe(vararg shardChannel: String, callback: MessageEventHandler) {
    shardChannel.forEach {
        subscriptions.registerSubscription(
            SubscribeTarget.Shard(it),
            callback.toPubSubHandler(this),
        )
    }
}

public suspend fun ReThis.sSubscribe(vararg shardChannel: String, callback: MessageBufferEventHandler) {
    shardChannel.forEach {
        subscriptions.registerSubscription(
            SubscribeTarget.Shard(it),
            callback.toPubSubHandler(this),
        )
    }
}
