package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("JSON.MERGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface JsonMergeCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        path: String,
        value: String
    ): CommandRequest
}
