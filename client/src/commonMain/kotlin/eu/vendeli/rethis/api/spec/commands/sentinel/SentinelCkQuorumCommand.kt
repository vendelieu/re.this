package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.sentinel.SentinelCkQuorumDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SENTINEL CKQUORUM", RedisOperation.READ, [RespCode.SIMPLE_STRING])
@RedisMeta.CustomCodec(decoder = SentinelCkQuorumDecoder::class)
fun interface SentinelCkQuorumCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(masterName: String): CommandRequest
}
