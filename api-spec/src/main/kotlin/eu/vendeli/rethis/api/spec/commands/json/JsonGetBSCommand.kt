package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.json.JsonGetOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand("JSON.GET", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface JsonGetBSCommand : RedisCommandSpec<ByteString> {
    suspend fun encode(
        key: String,
        @RIgnoreSpecAbsence vararg options: JsonGetOption,
    ): CommandRequest
}
