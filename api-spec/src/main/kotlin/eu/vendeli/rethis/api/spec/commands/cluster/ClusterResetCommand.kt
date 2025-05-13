package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.ClusterResetMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER RESET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [ClusterResetMode::class])
fun interface ClusterResetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(@RedisOptional mode: ClusterResetMode?): CommandRequest<Nothing>
}
