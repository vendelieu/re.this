package eu.vendeli.rethis.api.spec.commands.bf

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BF.LOADCHUNK", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface BfLoadChunkCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String, iterator: Long, data: String): CommandRequest
}
