package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BGSAVE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface BgSaveCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(@RedisOption.Token("SCHEDULE") schedule: Boolean?): CommandRequest
}
