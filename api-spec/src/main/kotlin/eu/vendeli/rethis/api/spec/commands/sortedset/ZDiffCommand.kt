package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.*
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZDIFF", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZDiffCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisMeta.WithSizeParam("numkeys") @RedisKey vararg key: String,
        @RedisOptional @RedisOption.Token("WITHSCORES") withscores: Boolean?,
    ): CommandRequest<List<String>>
}
