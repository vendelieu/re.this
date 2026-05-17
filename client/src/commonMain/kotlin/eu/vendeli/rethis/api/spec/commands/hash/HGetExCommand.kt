package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.hash.HGetExOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HGETEX", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface HGetExCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        expiration: HGetExOption?,
        @RedisOption.Token("FIELDS") @RedisMeta.WithSizeParam("numfields") vararg field: String,
    ): CommandRequest
}
