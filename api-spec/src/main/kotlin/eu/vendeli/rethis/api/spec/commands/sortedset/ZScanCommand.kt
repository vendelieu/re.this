package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZSCAN", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZScanCommand : RedisCommandSpec<ScanResult<Pair<String, String>>> {
    suspend fun encode(
        @RedisKey key: String,
        cursor: Long,
        @RedisOptional pattern: String?,
        @RedisOptional count: Long?
    ): CommandRequest<String>
}
