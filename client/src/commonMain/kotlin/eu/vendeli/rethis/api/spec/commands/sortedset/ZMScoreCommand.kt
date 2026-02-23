package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("ZMSCORE", RedisOperation.READ, [RespCode.ARRAY, RespCode.NULL])
fun interface ZMScoreCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        vararg member: String,
    ): CommandRequest
}
