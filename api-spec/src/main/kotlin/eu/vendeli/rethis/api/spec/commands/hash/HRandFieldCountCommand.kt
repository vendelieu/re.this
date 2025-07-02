package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("HRANDFIELD", RedisOperation.READ, [RespCode.ARRAY])
fun interface HRandFieldCountCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        count: Long,
        @RedisOption.Token("WITHVALUES") @RedisOption.Name("withvalues") withValues: Boolean?
    ): CommandRequest
}
