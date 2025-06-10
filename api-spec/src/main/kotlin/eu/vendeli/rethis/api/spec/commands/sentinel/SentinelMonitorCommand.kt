package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SENTINEL MONITOR", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface SentinelMonitorCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        masterName: String,
        ip: String,
        port: Int,
        quorum: Int
    ): CommandRequest
}
