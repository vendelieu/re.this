package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.string.LcsDecoder
import eu.vendeli.rethis.shared.request.string.LcsMode
import eu.vendeli.rethis.shared.request.string.MinMatchLen
import eu.vendeli.rethis.shared.response.string.LcsResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

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
