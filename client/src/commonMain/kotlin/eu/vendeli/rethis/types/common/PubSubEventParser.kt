package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.Push

object PubSubEventParser {
    fun parse(push: Push): PubSubEvent? {
        if (push.value.isEmpty()) return null
        val marker = (push.value[0] as? PlainString)?.value ?: return null

        return when (marker) {
            // ---------- Sub/Unsub ----------
            "subscribe" -> {
                PubSubEvent.Subscribed(
                    kind = PubSubKind.PLAIN,
                    target = SubscribeTarget.Channel(string(push, 1)),
                    activeCount = int(push, 2),
                )
            }

            "unsubscribe" -> {
                PubSubEvent.Unsubscribed(
                    kind = PubSubKind.PLAIN,
                    target = SubscribeTarget.Channel(string(push, 1)),
                    activeCount = int(push, 2),
                )
            }

            "psubscribe" -> {
                PubSubEvent.Subscribed(
                    kind = PubSubKind.PATTERN,
                    target = SubscribeTarget.Pattern(string(push, 1)),
                    activeCount = int(push, 2),
                )
            }

            "punsubscribe" -> {
                PubSubEvent.Unsubscribed(
                    kind = PubSubKind.PATTERN,
                    target = SubscribeTarget.Pattern(string(push, 1)),
                    activeCount = int(push, 2),
                )
            }

            "ssubscribe" -> {
                PubSubEvent.Subscribed(
                    kind = PubSubKind.SHARD,
                    target = SubscribeTarget.Shard(string(push, 1)),
                    activeCount = int(push, 2),
                )
            }

            "sunsubscribe" -> {
                PubSubEvent.Unsubscribed(
                    kind = PubSubKind.SHARD,
                    target = SubscribeTarget.Shard(string(push, 1)),
                    activeCount = int(push, 2),
                )
            }

            // ---------- Messages ----------
            "message" -> {
                PubSubEvent.Message(
                    channel = string(push, 1),
                    payload = push.value[2],
                    pattern = null,
                    kind = PubSubKind.PLAIN,
                )
            }

            "pmessage" -> {
                PubSubEvent.Message(
                    channel = string(push, 2),
                    payload = push.value[3],
                    pattern = string(push, 1),
                    kind = PubSubKind.PATTERN,
                )
            }

            "smessage" -> {
                PubSubEvent.Message(
                    channel = string(push, 1),
                    payload = push.value[2],
                    pattern = null,
                    kind = PubSubKind.SHARD,
                )
            }

            else -> {
                null
            }
        }
    }

    private fun string(push: Push, idx: Int) =
        (push.value[idx] as PlainString).value

    private fun int(push: Push, idx: Int) =
        (push.value[idx] as Int64).value
}
