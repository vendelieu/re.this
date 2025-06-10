package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.generic.RestoreOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("RESTORE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [RestoreOption::class])
fun interface RestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        ttl: Long,
        @RedisMeta.IgnoreCheck([ValidityCheck.TYPE]) serializedValue: ByteArray,
        @RedisOptional vararg options: RestoreOption,
    ): CommandRequest
}
