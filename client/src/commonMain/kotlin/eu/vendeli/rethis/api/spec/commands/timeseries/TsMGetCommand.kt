package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.MGET", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface TsMGetCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        @RedisOption.Token("LATEST") latest: Boolean?,
        @RedisOption.Token("WITHLABELS") withLabels: Boolean?,
        @RedisOption.Token("SELECTED_LABELS") selectedLabels: List<String>,
        @RedisOption.Token("FILTER") @RIgnoreSpecAbsence vararg filterExpr: String,
    ): CommandRequest
}
