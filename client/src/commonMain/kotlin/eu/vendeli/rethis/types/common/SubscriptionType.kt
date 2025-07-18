package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.codecs.pubsub.PSubscribeCommandCodec
import eu.vendeli.rethis.codecs.pubsub.SSubscribeCommandCodec
import eu.vendeli.rethis.codecs.pubsub.SubscribeCommandCodec
import io.ktor.utils.io.charsets.Charsets

sealed class SubscriptionType {
    internal abstract suspend fun request(id: String): CommandRequest
    internal abstract val regMarker: String
    internal abstract val unRegMarker: String
    internal abstract val messageMarker: String

    data object PLAIN : SubscriptionType() {
        override suspend fun request(id: String) = SubscribeCommandCodec.encode(Charsets.UTF_8, id)

        override val regMarker = "SUBSCRIBE"
        override val unRegMarker = "UNSUBSCRIBE"
        override val messageMarker = "message"
    }

    data object SHARD : SubscriptionType() {
        override suspend fun request(id: String) = SSubscribeCommandCodec.encode(Charsets.UTF_8, id)

        override val regMarker = "SSUBSCRIBE"
        override val unRegMarker = "SUNSUBSCRIBE"
        override val messageMarker = "smessage"
    }

    data object PATTERN : SubscriptionType() {
        override suspend fun request(id: String) = PSubscribeCommandCodec.encode(Charsets.UTF_8, id)

        override val regMarker = "PSUBSCRIBE"
        override val unRegMarker = "PUNSUBSCRIBE"
        override val messageMarker = "pmessage"
    }
}
