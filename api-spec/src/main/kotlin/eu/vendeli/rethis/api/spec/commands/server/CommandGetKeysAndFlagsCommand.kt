package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("COMMAND GETKEYSANDFLAGS", RedisOperation.READ, [RespCode.ARRAY])
fun interface CommandGetKeysAndFlagsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(vararg commandParts: String): CommandRequest<Nothing>
}
