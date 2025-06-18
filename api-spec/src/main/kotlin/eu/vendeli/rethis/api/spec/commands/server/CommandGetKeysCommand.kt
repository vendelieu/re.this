package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("COMMAND GETKEYS", RedisOperation.READ, [RespCode.ARRAY])
fun interface CommandGetKeysCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(command: String, vararg arg: String): CommandRequest
}
