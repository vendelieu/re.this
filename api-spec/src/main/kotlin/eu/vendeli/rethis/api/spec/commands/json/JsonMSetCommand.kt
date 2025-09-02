package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.json.JsonEntry
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("JSON.MSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface JsonMSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(vararg triplet: JsonEntry): CommandRequest
}
