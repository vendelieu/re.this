package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.common.LMPopDecoder
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZPopCommonOption
import eu.vendeli.rethis.api.spec.common.response.common.MPopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BZMPOP",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.NULL],
    isBlocking = true,
)
@RedisMeta.CustomCodec(decoder = LMPopDecoder::class)
fun interface BzMPopCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        timeout: Double,
        where: ZPopCommonOption,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest
}
