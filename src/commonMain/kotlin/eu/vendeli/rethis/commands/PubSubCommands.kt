package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.ChannelSubscription
import eu.vendeli.rethis.types.common.PubSubNumEntry
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.utils.registerSubscription

suspend fun ReThis.pSubscribe(vararg subscription: ChannelSubscription) = subscription.forEach {
    pSubscribe(it.channel, it.exceptionHandler, it.handler)
}

suspend fun ReThis.pSubscribe(
    pattern: String,
    exceptionHandler: ReThisExceptionHandler? = null,
    handler: SubscriptionHandler,
) {
    registerSubscription(
        regCommand = "PSUBSCRIBE",
        unRegCommand = "PUNSUBSCRIBE",
        exHandler = exceptionHandler,
        target = pattern,
        messageMarker = "message",
        handler = handler,
    )
}

suspend fun ReThis.publish(channel: String, message: String): Long = execute(
    listOf(
        "PUBLISH",
        channel,
        message,
    ),
).unwrap() ?: 0

suspend fun ReThis.pubSubChannels(pattern: String? = null): List<String> = execute(
    listOfNotNull(
        "PUBSUB",
        "CHANNELS",
        pattern,
    ),
).unwrapList()

suspend fun ReThis.pubSubNumPat(): Long = execute(
    listOf(
        "PUBSUB",
        "NUMPAT",
    ),
).unwrap() ?: 0

suspend fun ReThis.pubSubNumSub(vararg channel: String): List<PubSubNumEntry> = execute(
    listOf(
        "PUBSUB",
        "NUMSUB",
        *channel,
    ),
).unwrapList<RType>().chunked(2) {
    PubSubNumEntry(
        it.first().unwrap<String>()!!,
        it.last().unwrap() ?: 0,
    )
}

suspend fun ReThis.pubSubShardChannels(pattern: String? = null): List<String> = execute(
    listOfNotNull(
        "PUBSUB",
        "SHARDCHANNELS",
        pattern,
    ),
).unwrapList()

suspend fun ReThis.pubSubShardNumSub(vararg channel: String): List<PubSubNumEntry> = execute(
    listOf(
        "PUBSUB",
        "SHARDNUMSUB",
        *channel,
    ),
).unwrapList<RType>().chunked(2) {
    PubSubNumEntry(
        it.first().unwrap<String>()!!,
        it.last().unwrap() ?: 0,
    )
}

suspend fun ReThis.pUnsubscribe(vararg pattern: String): RType = execute(
    listOf(
        "PUNSUBSCRIBE",
        *pattern,
    ),
)

suspend fun ReThis.sPublish(shardChannel: String, message: String): Long? = execute(
    listOf(
        "SPUBLISH",
        shardChannel,
        message,
    ),
).unwrap()

suspend fun ReThis.sSubscribe(vararg subscription: ChannelSubscription) = subscription.forEach {
    sSubscribe(it.channel, it.exceptionHandler, it.handler)
}

suspend fun ReThis.sSubscribe(
    shardChannel: String,
    exceptionHandler: ReThisExceptionHandler? = null,
    handler: SubscriptionHandler,
) {
    registerSubscription(
        regCommand = "SSUBSCRIBE",
        unRegCommand = "SUNSUBSCRIBE",
        exHandler = exceptionHandler,
        target = shardChannel,
        messageMarker = "smessage",
        handler = handler,
    )
}

suspend fun ReThis.subscribe(vararg subscription: ChannelSubscription) = subscription.forEach {
    subscribe(it.channel, it.exceptionHandler, it.handler)
}

suspend fun ReThis.subscribe(
    channel: String,
    exceptionHandler: ReThisExceptionHandler? = null,
    handler: SubscriptionHandler,
) {
    registerSubscription(
        regCommand = "SUBSCRIBE",
        unRegCommand = "UNSUBSCRIBE",
        exHandler = exceptionHandler,
        target = channel,
        messageMarker = "message",
        handler = handler,
    )
}

suspend fun ReThis.sUnsubscribe(vararg pattern: String): RType = execute(
    listOf(
        "SUNSUBSCRIBE",
        *pattern,
    ),
)

suspend fun ReThis.unsubscribe(vararg pattern: String): RType = execute(
    listOf(
        "UNSUBSCRIBE",
        *pattern,
    ),
)
