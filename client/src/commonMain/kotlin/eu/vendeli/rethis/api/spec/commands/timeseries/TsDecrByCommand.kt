package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.timeseries.TsLabel
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.DECRBY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface TsDecrByCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        value: Double,
        @RedisOption.Token("TIMESTAMP") timestamp: String?,
        @RedisOption.Token("RETENTION") @RedisOption.Name("retentionPeriod") retention: Long?,
        @RedisOption.Token("UNCOMPRESSED") uncompressed: Boolean?,
        @RedisOption.Token("CHUNK_SIZE") @RedisOption.Name("size") chunkSize: Long?,
        @RedisOption.Token("LABELS") vararg labels: TsLabel,
    ): CommandRequest
}
