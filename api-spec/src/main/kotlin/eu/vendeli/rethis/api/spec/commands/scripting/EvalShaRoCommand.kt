package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation

@RedisCommand("EVALSHA_RO", RedisOperation.READ, [])
fun interface EvalShaRoCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        sha1: String,
        @RedisKey @RedisOptional @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOptional arg: List<String>
    ): CommandRequest<List<String>>
}
