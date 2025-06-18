package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.LcsDecoder
import eu.vendeli.rethis.api.spec.common.request.string.LcsMode
import eu.vendeli.rethis.api.spec.common.request.string.MinMatchLen
import eu.vendeli.rethis.api.spec.common.response.LcsResult
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
        mode: LcsMode.IDX,
        len: MinMatchLen?,
        @RedisOption.Token("WITHMATCHLEN") withMatchLen: Boolean?
    ): CommandRequest
}
