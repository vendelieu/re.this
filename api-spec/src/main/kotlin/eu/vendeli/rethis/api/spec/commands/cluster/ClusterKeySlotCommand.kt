package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLUSTER KEYSLOT", RedisOperation.READ, [RespCode.INTEGER])
fun interface ClusterKeySlotCommand : RedisCommandSpec<Long> {
    suspend fun encode(key: String): CommandRequest
}
