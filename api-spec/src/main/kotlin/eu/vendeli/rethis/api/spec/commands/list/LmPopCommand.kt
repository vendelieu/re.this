package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.common.LMPopDecoder
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LMPOP", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = LMPopDecoder::class)
fun interface LmPopCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        where: MoveDirection,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest
}
