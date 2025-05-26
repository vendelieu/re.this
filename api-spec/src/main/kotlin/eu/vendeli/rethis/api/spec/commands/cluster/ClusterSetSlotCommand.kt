package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.cluster.ClusterSetSlotOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER SETSLOT", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [ClusterSetSlotOption::class])
fun interface ClusterSetSlotCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(slot: Long, subcommand: ClusterSetSlotOption): CommandRequest<Nothing>
}
