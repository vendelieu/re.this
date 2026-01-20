package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.codecs.pubsub.PSubscribeCommandCodec
import eu.vendeli.rethis.codecs.pubsub.SSubscribeCommandCodec
import eu.vendeli.rethis.codecs.pubsub.SubscribeCommandCodec
import eu.vendeli.rethis.shared.types.CommandRequest
import io.ktor.utils.io.charsets.Charset

sealed class SubscribeTarget {
    data class Channel(
        val name: String,
    ) : SubscribeTarget()
    data class Pattern(
        val pattern: String,
    ) : SubscribeTarget()
    data class Shard(
        val name: String,
    ) : SubscribeTarget()
}

suspend fun SubscribeTarget.encode(charset: Charset): CommandRequest =
    when (this) {
        is SubscribeTarget.Channel -> {
            SubscribeCommandCodec.encode(charset, name)
        }

        is SubscribeTarget.Pattern -> {
            PSubscribeCommandCodec.encode(charset, pattern)
        }

        is SubscribeTarget.Shard -> {
            SSubscribeCommandCodec.encode(charset, name)
        }
    }
