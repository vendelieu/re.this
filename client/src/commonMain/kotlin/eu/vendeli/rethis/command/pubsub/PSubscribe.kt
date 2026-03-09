@file:OptIn(ExperimentalJsFileName::class)
@file:JsFileName("_PSubscribe.kt")

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

public suspend fun ReThis.pSubscribe(vararg pattern: String, callback: PubSubHandler) {
    pattern.forEach {
        subscriptions.registerSubscription(
            SubscribeTarget.Pattern(it),
            callback,
        )
    }
}

public suspend fun ReThis.pSubscribe(vararg pattern: String, callback: MessageEventHandler) {
    pattern.forEach {
        subscriptions.registerSubscription(
            SubscribeTarget.Pattern(it),
            callback.toPubSubHandler(this),
        )
    }
}

public suspend fun ReThis.pSubscribe(vararg pattern: String, callback: MessageBufferEventHandler) {
    pattern.forEach {
        subscriptions.registerSubscription(
            SubscribeTarget.Pattern(it),
            callback.toPubSubHandler(this),
        )
    }
}
