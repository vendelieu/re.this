package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("FCALL", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface FcallCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        name: String,
        @RedisKey @RedisOptional @RedisMeta.WithSizeParam("numkeys") vararg key: String,
    ): CommandRequest<List<String>>
}
