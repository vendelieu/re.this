package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.request.timeseries.TsAggregator
import eu.vendeli.rethis.shared.request.timeseries.TsValueRange
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.REVRANGE", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface TsRevRangeCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        @RedisOption.Name("fromTimestamp") fromTimestamp: String,
        @RedisOption.Name("toTimestamp") toTimestamp: String,
        @RedisOption.Token("LATEST") latest: Boolean?,
        @RedisOption.Token("FILTER_BY_TS") @RedisOption.Name("Timestamp") filterByTs: List<Long>,
        @RedisOption.Name("fbv") filterByValue: TsValueRange?,
        @RedisOption.Token("COUNT") count: Long?,
        @RedisOption.Token("ALIGN") @RedisOption.Name("value") align: Long?,
        @RedisOption.Name("aggregator") aggregator: TsAggregator?,
        @RedisOption.Name("bucketDuration") bucketDuration: Long?,
        @RedisOption.Token("BUCKETTIMESTAMP") @RedisOption.Name("buckettimestamp") bucketTimestamp: Boolean?,
        @RedisOption.Token("EMPTY") empty: Boolean?,
    ): CommandRequest
}
