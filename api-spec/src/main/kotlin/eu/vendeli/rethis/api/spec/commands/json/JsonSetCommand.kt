package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.request.string.UpsertMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.SET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface JsonSetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        value: String,
        @RedisMeta.Default("\"$\"") path: String,
        condition: UpsertMode?,
    ): CommandRequest
}
