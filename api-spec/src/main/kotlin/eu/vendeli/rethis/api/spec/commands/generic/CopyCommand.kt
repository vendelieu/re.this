package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.generic.CopyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("COPY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface CopyCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        source: String,
        destination: String,
        @RIgnoreSpecAbsence vararg option: CopyOption
    ): CommandRequest
}
