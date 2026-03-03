package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.utils.JSON_DEFAULT_PATH

@RedisCommand("JSON.DEL", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface JsonDelCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        @RedisMeta.Default("\"$JSON_DEFAULT_PATH\"") path: String?,
    ): CommandRequest
}
