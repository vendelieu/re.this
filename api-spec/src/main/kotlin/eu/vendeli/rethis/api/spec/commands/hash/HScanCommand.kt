package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.HScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("HSCAN", RedisOperation.READ, [RespCode.ARRAY], extensions = [HScanOption::class]) // todo custom encoder
@RedisMeta.IgnoreCheck([ValidityCheck.RESPONSE])
fun interface HScanCommand : RedisCommandSpec<ScanResult<Pair<String, String>>> {
    suspend fun encode(
        @RedisKey key: String,
        cursor: Long,
        @RedisOptional vararg option: HScanOption,
    ): CommandRequest<String>
}
