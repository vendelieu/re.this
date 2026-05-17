package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.server.SflushOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SFLUSH", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface SflushCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RIgnoreSpecAbsence vararg options: SflushOption,
    ): CommandRequest
}
