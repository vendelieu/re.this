package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.generic.RestoreOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

/**
 * @param ttl time to live in milliseconds (a unix timestamp in milliseconds with `ABSTTL`); `0` means no expiry.
 */
@RedisCommand("RESTORE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface RestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        ttl: Long,
        serializedValue: ByteArray,
        @RIgnoreSpecAbsence vararg options: RestoreOption,
    ): CommandRequest
}
