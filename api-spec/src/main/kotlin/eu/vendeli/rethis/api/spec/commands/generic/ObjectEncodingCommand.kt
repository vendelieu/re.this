package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("OBJECT ENCODING", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface ObjectEncodingCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String): CommandRequest
}
