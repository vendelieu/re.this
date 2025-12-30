package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("GETDEL", RedisOperation.WRITE, [RespCode.BULK, RespCode.NULL])
fun interface GetDelBACommand : RedisCommandSpec<ByteArray> {
    suspend fun encode(
        key: String
    ): CommandRequest
}
