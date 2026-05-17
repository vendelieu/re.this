package eu.vendeli.rethis.api.spec.commands.vector

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("VRANGE", RedisOperation.READ, [RespCode.ARRAY, RespCode.NULL])
fun interface VRangeCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        start: String,
        end: String,
        count: Long?,
    ): CommandRequest
}
