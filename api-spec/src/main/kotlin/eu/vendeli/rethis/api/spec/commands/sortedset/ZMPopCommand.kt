package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZPopCommonOption
import eu.vendeli.rethis.api.spec.common.response.MPopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZMPOP", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = ResponseDecoder::class) // todo add
fun interface ZMPopCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        where: ZPopCommonOption,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest
}
