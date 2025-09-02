package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.common.ArrayIntBooleanDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SMISMEMBER", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ArrayIntBooleanDecoder::class)
fun interface SMisMemberCommand : RedisCommandSpec<List<Boolean>> {
    suspend fun encode(
        key: String,
        vararg member: String,
    ): CommandRequest
}
