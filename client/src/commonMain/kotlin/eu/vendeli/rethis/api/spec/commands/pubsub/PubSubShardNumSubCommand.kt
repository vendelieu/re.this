package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.pubsub.PubSubNumSubDecoder
import eu.vendeli.rethis.shared.response.common.PubSubNumEntry
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("PUBSUB SHARDNUMSUB", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = PubSubNumSubDecoder::class)
fun interface PubSubShardNumSubCommand : RedisCommandSpec<List<PubSubNumEntry>> {
    suspend fun encode(vararg shardchannel: String): CommandRequest
}
