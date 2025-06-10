package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HEXPIRETIME", RedisOperation.READ, [RespCode.ARRAY])
fun interface HExpireTimeCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        @RedisMeta.WithSizeParam("numfields") vararg field: String
    ): CommandRequest
}
