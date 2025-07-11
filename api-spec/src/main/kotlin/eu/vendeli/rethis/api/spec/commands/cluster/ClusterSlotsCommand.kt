package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.cluster.ClusterSlotsDecoder
import eu.vendeli.rethis.api.spec.common.response.cluster.Cluster
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("CLUSTER SLOTS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ClusterSlotsDecoder::class)
fun interface ClusterSlotsCommand : RedisCommandSpec<Cluster> {
    suspend fun encode(): CommandRequest
}
