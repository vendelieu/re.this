package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.response.MPopResult
import eu.vendeli.rethis.api.spec.common.response.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LMPOP", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = ResponseDecoder::class) // todo add
fun interface LmPopCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        where: MoveDirection,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest
}
