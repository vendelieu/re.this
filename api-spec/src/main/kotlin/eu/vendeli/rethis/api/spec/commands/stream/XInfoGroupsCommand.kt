package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("XINFO GROUPS", RedisOperation.READ, [RespCode.ARRAY])
fun interface XInfoGroupsCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(key: String): CommandRequest
}
