package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand("HRANDFIELD", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface HRandFieldBSCommand : RedisCommandSpec<ByteString> {
    suspend fun encode(key: String): CommandRequest
}
