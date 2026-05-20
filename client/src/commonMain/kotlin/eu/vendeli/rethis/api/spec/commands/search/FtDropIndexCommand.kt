package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.DROPINDEX", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FtDropIndexCommand : RedisCommandSpec<String> {
    suspend fun encode(
        index: String,
        @RedisOption.Token("DD") deleteDocs: Boolean?,
    ): CommandRequest
}
