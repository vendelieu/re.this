package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.timeseries.TsEncoding
import eu.vendeli.rethis.shared.request.timeseries.TsLabel
import eu.vendeli.rethis.shared.request.timeseries.TsOnDuplicate
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.ADD", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface TsAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        timestamp: String,
        value: Double,
        @RedisOption.Token("RETENTION") @RedisOption.Name("retentionPeriod") retention: Long?,
        @RedisOption.Name("enc") encoding: TsEncoding?,
        @RedisOption.Token("CHUNK_SIZE") @RedisOption.Name("size") chunkSize: Long?,
        @RedisOption.Name("policy") onDuplicate: TsOnDuplicate?,
        @RedisOption.Token("LABELS") vararg labels: TsLabel,
    ): CommandRequest
}
