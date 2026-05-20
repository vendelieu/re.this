package eu.vendeli.rethis.api.spec.commands.cf

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CF.LOADCHUNK", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface CfLoadChunkCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String, iterator: Long, data: String): CommandRequest
}
