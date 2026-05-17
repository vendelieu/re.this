package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HGETDEL", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface HGetDelCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("FIELDS") @RedisMeta.WithSizeParam("numfields") vararg field: String,
    ): CommandRequest
}
