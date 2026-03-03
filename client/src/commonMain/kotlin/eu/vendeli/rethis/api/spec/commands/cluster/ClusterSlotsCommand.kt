package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.cluster.ClusterSlotsDecoder
import eu.vendeli.rethis.shared.response.cluster.Cluster
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLUSTER SLOTS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ClusterSlotsDecoder::class)
fun interface ClusterSlotsCommand : RedisCommandSpec<Cluster> {
    suspend fun encode(): CommandRequest
}
