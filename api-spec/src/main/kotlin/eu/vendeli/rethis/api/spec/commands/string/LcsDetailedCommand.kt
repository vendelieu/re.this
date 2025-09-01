package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.string.LcsDecoder
import eu.vendeli.rethis.api.spec.common.request.string.LcsMode
import eu.vendeli.rethis.api.spec.common.request.string.MinMatchLen
import eu.vendeli.rethis.api.spec.common.response.string.LcsResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "LCS", 
    RedisOperation.READ,
    [RespCode.MAP, RespCode.ARRAY],
)
@RedisMeta.CustomCodec(decoder = LcsDecoder::class)
fun interface LcsDetailedCommand : RedisCommandSpec<LcsResult> {
    suspend fun encode(
        key1: String,
        key2: String,
        @RIgnoreSpecAbsence mode: LcsMode.IDX,
        minMatchLen: MinMatchLen?,
        @RedisOption.Token("WITHMATCHLEN") @RedisOption.Name("withmatchlen") withMatchLen: Boolean?
    ): CommandRequest
}
