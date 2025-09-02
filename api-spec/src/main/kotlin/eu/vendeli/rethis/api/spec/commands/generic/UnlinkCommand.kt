package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("UNLINK", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface UnlinkCommand : RedisCommandSpec<Long> {
    suspend fun encode(vararg key: String): CommandRequest
}
