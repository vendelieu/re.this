package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.list.LInsertPlace
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LINSERT", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface LInsertCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        where: LInsertPlace,
        pivot: String,
        element: String,
    ): CommandRequest
}
