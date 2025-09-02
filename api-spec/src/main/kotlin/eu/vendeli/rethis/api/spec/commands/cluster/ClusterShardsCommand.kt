package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.cluster.ClusterShardsDecoder
import eu.vendeli.rethis.shared.response.cluster.Shard
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLUSTER SHARDS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ClusterShardsDecoder::class)
fun interface ClusterShardsCommand : RedisCommandSpec<List<Shard>> {
    suspend fun encode(): CommandRequest
}
