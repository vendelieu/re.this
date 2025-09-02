package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("SLOWLOG GET", RedisOperation.READ, [RespCode.ARRAY])
fun interface SlowLogGetCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(count: Long?): CommandRequest
}
