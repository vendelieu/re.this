package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("COMMAND GETKEYSANDFLAGS", RedisOperation.READ, [RespCode.ARRAY])
fun interface CommandGetKeysAndFlagsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(vararg commandParts: String): CommandRequest<Nothing>
}
