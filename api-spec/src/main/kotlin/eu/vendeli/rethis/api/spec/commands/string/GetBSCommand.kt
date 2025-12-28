package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand("GET", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface GetBSCommand : RedisCommandSpec<ByteString> {
    suspend fun encode(
        key: String
    ): CommandRequest
}
