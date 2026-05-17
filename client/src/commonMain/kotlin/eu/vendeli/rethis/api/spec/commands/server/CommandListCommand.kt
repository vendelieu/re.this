package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.server.CommandListFilter
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("COMMAND LIST", RedisOperation.READ, [RespCode.ARRAY])
fun interface CommandListCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(filterby: CommandListFilter?): CommandRequest
}
