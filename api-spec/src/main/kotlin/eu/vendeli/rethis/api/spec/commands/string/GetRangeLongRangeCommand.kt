package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("GETRANGE", RedisOperation.READ, [RespCode.BULK])
fun interface GetRangeLongRangeCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey key: String,
        start: Long,
        end: Long,
    ): CommandRequest<String>
}
