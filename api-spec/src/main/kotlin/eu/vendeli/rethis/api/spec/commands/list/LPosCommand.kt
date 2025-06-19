package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.list.LPosOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LPOS", RedisOperation.READ, [RespCode.INTEGER, RespCode.NULL])
fun interface LPosCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        element: String,
        @RIgnoreSpecAbsence vararg option: LPosOption,
    ): CommandRequest
}
