package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SENTINEL GET-MASTER-ADDR-BY-NAME", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelGetMasterAddrCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(masterName: String): CommandRequest
}
