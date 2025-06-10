package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SENTINEL GET-MASTER-ADDR-BY-NAME", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelGetMasterAddrCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(masterName: String): CommandRequest
}
