package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("EVALSHA", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface EvalShaCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        sha1: String,
        numKeys: Long,
        @RedisKey @RedisMeta.WithSizeParam("numkeys") @RedisOptional vararg key: String
    ): CommandRequest<List<String>>
}
