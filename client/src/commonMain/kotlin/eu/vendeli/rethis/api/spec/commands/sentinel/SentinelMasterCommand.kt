package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("SENTINEL MASTER", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelMasterCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(masterName: String): CommandRequest
}
