package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.response.PubSubNumEntry
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PUBSUB SHARDNUMSUB", RedisOperation.READ, [RespCode.ARRAY])
fun interface PubSubShardNumSubCommand : RedisCommandSpec<List<PubSubNumEntry>> {
    suspend fun encode(@RedisOptional vararg shardchannel: String): CommandRequest
}
