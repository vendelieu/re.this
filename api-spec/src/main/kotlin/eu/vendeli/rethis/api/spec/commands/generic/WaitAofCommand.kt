package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.generic.WaitAofDecoder
import eu.vendeli.rethis.api.spec.common.response.common.WaitAofResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("WAITAOF", RedisOperation.WRITE, [RespCode.ARRAY], isBlocking = true)
@RedisMeta.CustomCodec(decoder = WaitAofDecoder::class)
fun interface WaitAofCommand : RedisCommandSpec<WaitAofResult> {
    suspend fun encode(
        numlocal: Long,
        numreplicas: Long,
        timeout: Long,
    ): CommandRequest
}
