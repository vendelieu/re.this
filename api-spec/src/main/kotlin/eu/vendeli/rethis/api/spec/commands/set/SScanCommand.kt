package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.SScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SSCAN", RedisOperation.READ, [RespCode.ARRAY], extensions = [SScanOption::class])
fun interface SScanCommand : RedisCommandSpec<ScanResult<String>> {
    suspend fun encode(
        @RedisKey key: String,
        cursor: Long,
        vararg option: SScanOption,
    ): CommandRequest<String>
}
