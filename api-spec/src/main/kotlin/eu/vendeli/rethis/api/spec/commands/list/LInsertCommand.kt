package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.list.LInsertPlace
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LINSERT", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [LInsertPlace::class])
fun interface LInsertCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        where: LInsertPlace,
        pivot: String,
        element: String,
    ): CommandRequest<String>
}
