package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.response.WaitAofResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("WAITAOF", RedisOperation.WRITE, [RespCode.ARRAY], isBlocking = true)
@RedisMeta.CustomCodec(decoder = Nothing::class) // todo add
fun interface WaitAofCommand : RedisCommandSpec<WaitAofResult> {
    suspend fun encode(
        numlocal: Long,
        numreplicas: Long,
        timeout: Long
    ): CommandRequest
}
