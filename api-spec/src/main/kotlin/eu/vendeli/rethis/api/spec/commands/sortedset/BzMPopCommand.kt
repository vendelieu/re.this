package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.common.LMPopDecoder
import eu.vendeli.rethis.shared.request.sortedset.ZPopCommonOption
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

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
