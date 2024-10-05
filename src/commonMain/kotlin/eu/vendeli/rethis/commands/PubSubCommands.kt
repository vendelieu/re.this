package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.ChannelSubscription
import eu.vendeli.rethis.types.common.PubSubNumEntry
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.utils.registerSubscription
import eu.vendeli.rethis.utils.writeArg

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
        "PUBLISH".toArg(),
        channel.toArg(),
        message.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.pubSubChannels(pattern: String? = null): List<String> = execute(
    mutableListOf(
        "PUBSUB".toArg(),
        "CHANNELS".toArg(),
    ).writeArg(pattern),
).unwrapList()

suspend fun ReThis.pubSubNumPat(): Long = execute(
    listOf(
        "PUBSUB".toArg(),
        "NUMPAT".toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.pubSubNumSub(vararg channel: String): List<PubSubNumEntry> = execute(
    listOf(
        "PUBSUB".toArg(),
        "NUMSUB".toArg(),
        *channel.toArg(),
    ),
).unwrapList<RType>().chunked(2) {
    PubSubNumEntry(
        it.first().unwrap<String>()!!,
        it.last().unwrap() ?: 0,
    )
}

suspend fun ReThis.pubSubShardChannels(pattern: String? = null): List<String> = execute(
    mutableListOf(
        "PUBSUB".toArg(),
        "SHARDCHANNELS".toArg(),
    ).writeArg(pattern),
).unwrapList()

suspend fun ReThis.pubSubShardNumSub(vararg channel: String): List<PubSubNumEntry> = execute(
    listOf(
        "PUBSUB".toArg(),
        "SHARDNUMSUB".toArg(),
        *channel.toArg(),
    ),
).unwrapList<RType>().chunked(2) {
    PubSubNumEntry(
        it.first().unwrap<String>()!!,
        it.last().unwrap() ?: 0,
    )
}

suspend fun ReThis.pUnsubscribe(vararg pattern: String): RType = execute(
    listOf(
        "PUNSUBSCRIBE".toArg(),
        *pattern.toArg(),
    ),
)

suspend fun ReThis.sPublish(shardChannel: String, message: String): Long? = execute(
    listOf(
        "SPUBLISH".toArg(),
        shardChannel.toArg(),
        message.toArg(),
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
        "SUNSUBSCRIBE".toArg(),
        *pattern.toArg(),
    ),
)

suspend fun ReThis.unsubscribe(vararg pattern: String): RType = execute(
    listOf(
        "UNSUBSCRIBE".toArg(),
        *pattern.toArg(),
    ),
)
