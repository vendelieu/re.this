package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.generic.CopyOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("COPY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface CopyCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        source: String,
        destination: String,
        @RIgnoreSpecAbsence vararg option: CopyOption
    ): CommandRequest
}
