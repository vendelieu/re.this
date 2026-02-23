package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.request.string.UpsertMode
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("JSON.SET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface JsonSetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        value: String,
        @RedisMeta.Default("\"$\"") path: String,
        condition: UpsertMode?,
    ): CommandRequest
}
