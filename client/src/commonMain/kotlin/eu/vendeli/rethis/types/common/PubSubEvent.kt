package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.shared.types.RType

enum class PubSubKind {
    PLAIN,
    PATTERN,
    SHARD,
}

sealed class PubSubEvent {
    abstract val kind: PubSubKind

    data class Subscribed(
        override val kind: PubSubKind,
        val target: SubscribeTarget,
        val activeCount: Long,
    ) : PubSubEvent()

    data class Unsubscribed(
        override val kind: PubSubKind,
        val target: SubscribeTarget,
        val activeCount: Long,
    ) : PubSubEvent()

    data class Message(
        val channel: String,
        val payload: RType,
        val pattern: String? = null, // non-null only for pattern subscriptions
        override val kind: PubSubKind,
    ) : PubSubEvent()
}
