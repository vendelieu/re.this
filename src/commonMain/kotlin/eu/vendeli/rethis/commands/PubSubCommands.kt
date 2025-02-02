package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.ChannelSubscription
import eu.vendeli.rethis.types.common.PubSubNumEntry
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.utils.registerSubscription
import eu.vendeli.rethis.utils.writeArgument
import kotlin.Long
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.pSubscribe(vararg subscription: ChannelSubscription) = subscription.forEach {
    pSubscribe(it.channel, it.handler)
}

suspend fun ReThis.pSubscribe(
    pattern: String,
    handler: SubscriptionHandler,
) {
    registerSubscription(
        regCommand = "PSUBSCRIBE",
        unRegCommand = "PUNSUBSCRIBE",
        target = pattern,
        messageMarker = "pmessage",
        handler = handler,
    )
}

suspend fun ReThis.publish(channel: String, message: String): Long = execute<Long>(
    listOf(
        "PUBLISH".toArgument(),
        channel.toArgument(),
        message.toArgument(),
    ),
) ?: 0

suspend fun ReThis.pubSubChannels(pattern: String? = null): List<String> = execute(
    mutableListOf(
        "PUBSUB".toArgument(),
        "CHANNELS".toArgument(),
    ).writeArgument(pattern),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.pubSubNumPat(): Long = execute<Long>(
    listOf(
        "PUBSUB".toArgument(),
        "NUMPAT".toArgument(),
    ),
) ?: 0

suspend fun ReThis.pubSubNumSub(vararg channel: String): List<PubSubNumEntry> = execute(
    listOf(
        "PUBSUB".toArgument(),
        "NUMSUB".toArgument(),
        *channel.toArgument(),
    ),
).unwrapList<RType>().chunked(2) {
    PubSubNumEntry(
        it.first().unwrap<String>()!!,
        it.last().unwrap() ?: 0,
    )
}

suspend fun ReThis.pubSubShardChannels(pattern: String? = null): List<String> = execute(
    mutableListOf(
        "PUBSUB".toArgument(),
        "SHARDCHANNELS".toArgument(),
    ).writeArgument(pattern),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.pubSubShardNumSub(vararg channel: String): List<PubSubNumEntry> = execute(
    listOf(
        "PUBSUB".toArgument(),
        "SHARDNUMSUB".toArgument(),
        *channel.toArgument(),
    ),
).unwrapList<RType>().chunked(2) {
    PubSubNumEntry(
        it.first().unwrap<String>()!!,
        it.last().unwrap() ?: 0,
    )
}

suspend fun ReThis.pUnsubscribe(vararg pattern: String): RType {
    require(pattern.isNotEmpty())
    pattern.forEach { subscriptions.unsubscribe(it) }
    return execute(
        listOf(
            "PUNSUBSCRIBE".toArgument(),
            *pattern.toArgument(),
        ),
    )
}

suspend fun ReThis.sPublish(shardChannel: String, message: String): Long? = execute<Long>(
    listOf(
        "SPUBLISH".toArgument(),
        shardChannel.toArgument(),
        message.toArgument(),
    ),
)

suspend fun ReThis.sSubscribe(vararg subscription: ChannelSubscription) = subscription.forEach {
    sSubscribe(it.channel, it.handler)
}

suspend fun ReThis.sSubscribe(
    shardChannel: String,
    handler: SubscriptionHandler,
) {
    registerSubscription(
        regCommand = "SSUBSCRIBE",
        unRegCommand = "SUNSUBSCRIBE",
        target = shardChannel,
        messageMarker = "smessage",
        handler = handler,
    )
}

suspend fun ReThis.subscribe(vararg subscription: ChannelSubscription) = subscription.forEach {
    subscribe(it.channel, it.handler)
}

suspend fun ReThis.subscribe(
    channel: String,
    handler: SubscriptionHandler,
) {
    registerSubscription(
        regCommand = "SUBSCRIBE",
        unRegCommand = "UNSUBSCRIBE",
        target = channel,
        messageMarker = "message",
        handler = handler,
    )
}

suspend fun ReThis.sUnsubscribe(vararg pattern: String): RType {
    require(pattern.isNotEmpty())
    pattern.forEach { subscriptions.unsubscribe(it) }
    return execute(
        listOf(
            "SUNSUBSCRIBE".toArgument(),
            *pattern.toArgument(),
        ),
    )
}

suspend fun ReThis.unsubscribe(vararg pattern: String): RType {
    require(pattern.isNotEmpty())
    pattern.forEach { subscriptions.unsubscribe(it) }
    return execute(
        listOf(
            "UNSUBSCRIBE".toArgument(),
            *pattern.toArgument(),
        ),
    )
}
