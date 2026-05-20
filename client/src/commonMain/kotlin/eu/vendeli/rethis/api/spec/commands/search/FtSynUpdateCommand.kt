package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.SYNUPDATE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FtSynUpdateCommand : RedisCommandSpec<String> {
    suspend fun encode(
        index: String,
        @RedisOption.Name("synonym_group_id") synonymGroupId: String,
        @RedisOption.Token("SKIPINITIALSCAN") skipInitialScan: Boolean?,
        vararg term: String,
    ): CommandRequest
}
