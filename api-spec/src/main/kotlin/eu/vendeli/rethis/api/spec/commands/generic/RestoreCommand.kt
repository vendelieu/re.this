package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.RestoreOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("RESTORE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [RestoreOption::class])
fun interface RestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisKey key: String,
        ttl: Long,
        serializedValue: ByteArray,
        vararg options: RestoreOption
    ): CommandRequest<String>
}
