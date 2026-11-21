package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.sentinel.SentinelResetDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SENTINEL RESET", RedisOperation.WRITE, [RespCode.INTEGER])
@RedisMeta.CustomCodec(decoder = SentinelResetDecoder::class)
fun interface SentinelResetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(pattern: String): CommandRequest
}
