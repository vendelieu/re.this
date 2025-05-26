package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.generic.ScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("SCAN", RedisOperation.READ, [RespCode.ARRAY], extensions = [ScanOption::class])
@RedisMeta.IgnoreCheck([ValidityCheck.RESPONSE])
fun interface ScanCommand : RedisCommandSpec<ScanResult<String>> {
    suspend fun encode(
        cursor: Long,
        @RedisOptional vararg option: ScanOption,
    ): CommandRequest<Nothing>
}
