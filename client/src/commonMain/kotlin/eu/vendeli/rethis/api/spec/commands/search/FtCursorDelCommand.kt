package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.CURSOR DEL", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FtCursorDelCommand : RedisCommandSpec<String> {
    suspend fun encode(
        index: String,
        @RedisOption.Name("cursor_id") cursorId: Long,
    ): CommandRequest
}
