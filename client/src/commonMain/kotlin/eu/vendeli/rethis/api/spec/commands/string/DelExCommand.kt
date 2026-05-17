package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.string.DelExOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("DELEX", RedisOperation.WRITE, [RespCode.INTEGER, RespCode.BOOLEAN])
fun interface DelExCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(key: String, condition: DelExOption?): CommandRequest
}
