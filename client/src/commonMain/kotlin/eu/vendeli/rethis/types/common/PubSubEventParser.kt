package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.shared.types.Push
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.utils.unwrap

object PubSubEventParser {
    /**
     * Returns `null` for unknown markers and for frames that are too short or carry fields of the wrong type.
     */
    fun parse(push: Push): PubSubEvent? {
        val marker = push.value.firstOrNull()?.unwrap<String>() ?: return null

        return when (marker) {
            "subscribe" -> subscription(push, PubSubKind.PLAIN, subscribed = true)
            "unsubscribe" -> subscription(push, PubSubKind.PLAIN, subscribed = false)
            "psubscribe" -> subscription(push, PubSubKind.PATTERN, subscribed = true)
            "punsubscribe" -> subscription(push, PubSubKind.PATTERN, subscribed = false)
            "ssubscribe" -> subscription(push, PubSubKind.SHARD, subscribed = true)
            "sunsubscribe" -> subscription(push, PubSubKind.SHARD, subscribed = false)
            "message" -> message(push, PubSubKind.PLAIN)
            "pmessage" -> message(push, PubSubKind.PATTERN)
            "smessage" -> message(push, PubSubKind.SHARD)
            else -> null
        }
    }

    // ---------- Sub/Unsub ----------
    private fun subscription(push: Push, kind: PubSubKind, subscribed: Boolean): PubSubEvent? {
        val name = string(push, 1) ?: return null
        val activeCount = push.value.getOrNull(2)?.unwrap<Long>() ?: return null
        val target = when (kind) {
            PubSubKind.PLAIN -> SubscribeTarget.Channel(name)
            PubSubKind.PATTERN -> SubscribeTarget.Pattern(name)
            PubSubKind.SHARD -> SubscribeTarget.Shard(name)
        }

        return if (subscribed) {
            PubSubEvent.Subscribed(kind, target, activeCount)
        } else {
            PubSubEvent.Unsubscribed(kind, target, activeCount)
        }
    }

    // ---------- Messages ----------
    private fun message(push: Push, kind: PubSubKind): PubSubEvent? {
        val isPattern = kind == PubSubKind.PATTERN
        val channelIdx = if (isPattern) 2 else 1
        val channel = string(push, channelIdx) ?: return null
        val pattern = if (isPattern) string(push, 1) ?: return null else null
        val payload: RType = push.value.getOrNull(channelIdx + 1) ?: return null

        return PubSubEvent.Message(channel = channel, payload = payload, pattern = pattern, kind = kind)
    }

    private fun string(push: Push, idx: Int) = push.value.getOrNull(idx)?.unwrap<String>()
}
