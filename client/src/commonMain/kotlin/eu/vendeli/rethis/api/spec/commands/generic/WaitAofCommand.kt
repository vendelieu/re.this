package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.generic.WaitAofDecoder
import eu.vendeli.rethis.shared.response.common.WaitAofResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

/**
 * @param timeout maximum time to block in milliseconds; `0` blocks forever.
 */
@RedisCommand("WAITAOF", RedisOperation.WRITE, [RespCode.ARRAY], isBlocking = true)
@RedisMeta.CustomCodec(decoder = WaitAofDecoder::class)
fun interface WaitAofCommand : RedisCommandSpec<WaitAofResult> {
    suspend fun encode(
        numlocal: Long,
        numreplicas: Long,
        timeout: Long,
    ): CommandRequest
}
