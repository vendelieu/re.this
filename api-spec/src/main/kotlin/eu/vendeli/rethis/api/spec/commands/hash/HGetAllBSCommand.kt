package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand("HGETALL", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface HGetAllBSCommand : RedisCommandSpec<Map<String, ByteString?>> {
    suspend fun encode(key: String): CommandRequest
}
