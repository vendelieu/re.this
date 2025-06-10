package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.MERGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface JsonMergeCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        path: String,
        value: String
    ): CommandRequest
}
