package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XClaimOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XCLAIM", RedisOperation.WRITE, [RespCode.ARRAY], extensions = [XClaimOption::class])
fun interface XClaimCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        vararg id: String,
        @RedisOptional idle: XClaimOption.Idle?,
        @RedisOptional time: XClaimOption.Time?,
        @RedisOptional retryCount: XClaimOption.RetryCount?,
        @RedisOptional force: Boolean?,
        @RedisOptional justID: Boolean?,
        @RedisOptional lastId: XClaimOption.LastId?
    ): CommandRequest<String>
}
