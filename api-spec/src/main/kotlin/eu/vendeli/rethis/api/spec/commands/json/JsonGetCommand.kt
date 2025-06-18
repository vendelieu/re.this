package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.json.JsonGetOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.GET", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface JsonGetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        vararg options: JsonGetOption,
    ): CommandRequest
}
