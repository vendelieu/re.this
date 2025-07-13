package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("SENTINEL MASTER", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelMasterCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(masterName: String): CommandRequest
}
