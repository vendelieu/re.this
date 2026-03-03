package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.utils.JSON_DEFAULT_PATH

@RedisCommand("JSON.MERGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface JsonMergeCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        value: String,
        @RedisMeta.Default("\"$JSON_DEFAULT_PATH\"") path: String,
    ): CommandRequest
}
