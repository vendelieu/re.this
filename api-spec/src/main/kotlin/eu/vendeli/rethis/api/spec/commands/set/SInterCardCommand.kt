package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SINTERCARD", RedisOperation.READ, [RespCode.INTEGER])
fun interface SInterCardCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOptional @RedisOption.Token("LIMIT") limit: Long?,
    ): CommandRequest<List<String>>
}
