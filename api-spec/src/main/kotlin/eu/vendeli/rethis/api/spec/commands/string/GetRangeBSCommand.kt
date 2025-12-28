package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand("GETRANGE", RedisOperation.READ, [RespCode.BULK])
fun interface GetRangeBSCommand : RedisCommandSpec<ByteString> {
    suspend fun encode(
        key: String,
        start: Long,
        end: Long
    ): CommandRequest
}
