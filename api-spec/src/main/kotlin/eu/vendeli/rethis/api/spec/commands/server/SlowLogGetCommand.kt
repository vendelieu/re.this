package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("SLOWLOG GET", RedisOperation.READ, [RespCode.ARRAY])
fun interface SlowLogGetCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(@RedisOptional count: Long?): CommandRequest
}
