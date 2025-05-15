package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.LcsMode
import eu.vendeli.rethis.api.spec.common.request.MinMatchLen
import eu.vendeli.rethis.api.spec.common.response.LcsResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "LCS", 
    RedisOperation.READ,
    [RespCode.MAP, RespCode.ARRAY],
    extensions = [LcsMode.IDX::class, MinMatchLen::class]
)
fun interface LcsDetailedCommand : RedisCommandSpec<LcsResult> {
    suspend fun encode(
        @RedisKey key1: String,
        @RedisKey key2: String,
        mode: LcsMode.IDX,
        @RedisOptional len: MinMatchLen?,
        @RedisOptional @RedisOption.Token("WITHMATCHLEN") withMatchLen: Boolean?
    ): CommandRequest<List<String>>
}
