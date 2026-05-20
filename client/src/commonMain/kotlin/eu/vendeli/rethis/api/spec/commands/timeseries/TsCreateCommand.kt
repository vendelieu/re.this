package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.timeseries.TsDuplicatePolicy
import eu.vendeli.rethis.shared.request.timeseries.TsEncoding
import eu.vendeli.rethis.shared.request.timeseries.TsLabel
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.CREATE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface TsCreateCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("RETENTION") @RedisOption.Name("retentionPeriod") retention: Long?,
        @RedisOption.Name("enc") encoding: TsEncoding?,
        @RedisOption.Token("CHUNK_SIZE") @RedisOption.Name("size") chunkSize: Long?,
        @RedisOption.Name("policy") duplicatePolicy: TsDuplicatePolicy?,
        @RedisOption.Token("LABELS") vararg labels: TsLabel,
    ): CommandRequest
}
