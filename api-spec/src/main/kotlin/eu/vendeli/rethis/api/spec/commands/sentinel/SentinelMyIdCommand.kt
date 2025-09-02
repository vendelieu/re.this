package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SENTINEL MYID", RedisOperation.READ, [RespCode.BULK])
fun interface SentinelMyIdCommand : RedisCommandSpec<String> {
    suspend fun encode(): CommandRequest
}
