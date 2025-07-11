package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.cluster.ClusterShardsDecoder
import eu.vendeli.rethis.api.spec.common.response.cluster.Shard
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER SHARDS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ClusterShardsDecoder::class)
fun interface ClusterShardsCommand : RedisCommandSpec<List<Shard>> {
    suspend fun encode(): CommandRequest
}
