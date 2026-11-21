package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.cluster.ClusterBumpEpochDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "CLUSTER BUMPEPOCH",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING, RespCode.BULK, RespCode.VERBATIM_STRING],
)
@RedisMeta.CustomCodec(decoder = ClusterBumpEpochDecoder::class)
fun interface ClusterBumpEpochCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(): CommandRequest
}
