package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("MODULE LIST", RedisOperation.READ, [RespCode.ARRAY])
fun interface ModuleListCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(): CommandRequest
}
