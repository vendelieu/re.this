package eu.vendeli.rethis.api.spec.commands.vector

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.vector.VAddInput
import eu.vendeli.rethis.shared.request.vector.VAddOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("VADD", RedisOperation.WRITE, [RespCode.INTEGER, RespCode.BOOLEAN])
fun interface VAddCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        @RIgnoreSpecAbsence input: VAddInput,
        element: String,
        @RIgnoreSpecAbsence vararg options: VAddOption,
    ): CommandRequest
}
