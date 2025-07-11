package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SMISMEMBER", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ResponseDecoder::class) // todo add array<boolean> 1 - true, 0 - false
fun interface SMisMemberCommand : RedisCommandSpec<List<Boolean>> {
    suspend fun encode(
        key: String,
        vararg member: String,
    ): CommandRequest
}
