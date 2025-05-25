package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.SlotRange
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER ADDSLOTSRANGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [SlotRange::class])
fun interface ClusterAddSlotsRangeCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(vararg range: SlotRange): CommandRequest<Nothing>
}
