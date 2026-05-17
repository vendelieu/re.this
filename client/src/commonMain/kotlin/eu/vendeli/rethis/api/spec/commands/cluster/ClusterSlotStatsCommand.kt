package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.request.cluster.ClusterSlotStatsFilter
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLUSTER SLOT-STATS", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface ClusterSlotStatsCommand : RedisCommandSpec<RType> {
    suspend fun encode(filter: ClusterSlotStatsFilter): CommandRequest
}
