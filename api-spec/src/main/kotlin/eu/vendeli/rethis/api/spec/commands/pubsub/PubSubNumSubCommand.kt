package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.pubsub.PubSubNumSubDecoder
import eu.vendeli.rethis.api.spec.common.response.common.PubSubNumEntry
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PUBSUB NUMSUB", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = PubSubNumSubDecoder::class)
fun interface PubSubNumSubCommand : RedisCommandSpec<List<PubSubNumEntry>> {
    suspend fun encode(vararg channel: String): CommandRequest
}
