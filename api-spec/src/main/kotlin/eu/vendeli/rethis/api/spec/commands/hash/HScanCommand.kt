package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.HScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HSCAN", RedisOperation.READ, [RespCode.ARRAY], extensions = [HScanOption::class, ScanResult::class])
fun interface HScanCommand : RedisCommandSpec<ScanResult<Pair<String, String>>> {
    suspend fun encode(
        @RedisKey key: String,
        cursor: Long,
        vararg option: HScanOption
    ): CommandRequest<String>
}
