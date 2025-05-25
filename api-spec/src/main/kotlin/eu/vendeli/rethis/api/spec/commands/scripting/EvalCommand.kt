package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("EVAL", RedisOperation.WRITE, [])
fun interface EvalCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        script: String,
        @RedisKey @RedisOptional @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOptional arg: List<String> ,
    ): CommandRequest<List<String>>
}
