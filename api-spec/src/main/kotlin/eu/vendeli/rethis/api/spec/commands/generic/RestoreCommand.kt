package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.request.generic.RestoreOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("RESTORE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface RestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        ttl: Long,
        @RedisMeta.IgnoreCheck([ValidityCheck.TYPE]) serializedValue: ByteArray,
        @RIgnoreSpecAbsence vararg options: RestoreOption,
    ): CommandRequest
}
