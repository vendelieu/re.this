package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.string.SetOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand(
    "SET",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.SIMPLE_STRING, RespCode.NULL],
)
fun interface SetBSCommand : RedisCommandSpec<ByteString> {
    suspend fun encode(
        key: String,
        value: ByteString,
        @RIgnoreSpecAbsence vararg options: SetOption,
    ): CommandRequest
}
