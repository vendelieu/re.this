package eu.vendeli.rethis.api.spec.commands.tdigest

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TDIGEST.TRIMMED_MEAN", RedisOperation.READ, [RespCode.BULK, RespCode.DOUBLE])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface TDigestTrimmedMeanCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        @RedisOption.Name("low_cut_quantile") lowCutQuantile: Double,
        @RedisOption.Name("high_cut_quantile") highCutQuantile: Double,
    ): CommandRequest
}
