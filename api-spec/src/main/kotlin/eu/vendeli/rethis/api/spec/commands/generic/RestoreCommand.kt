package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.generic.RestoreOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("RESTORE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface RestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        ttl: Long,
        serializedValue: ByteArray,
        @RIgnoreSpecAbsence vararg options: RestoreOption,
    ): CommandRequest
}
