package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("COMMAND GETKEYS", RedisOperation.READ, [RespCode.ARRAY])
fun interface CommandGetKeysCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(command: String, vararg arg: String): CommandRequest
}
