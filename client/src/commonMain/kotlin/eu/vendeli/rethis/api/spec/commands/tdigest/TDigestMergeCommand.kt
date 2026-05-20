package eu.vendeli.rethis.api.spec.commands.tdigest

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TDIGEST.MERGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface TDigestMergeCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisOption.Name("destination-key") destinationKey: String,
        @RedisMeta.WithSizeParam("numkeys") vararg sourceKey: String,
        @RedisOption.Token("COMPRESSION") compression: Long?,
        @RedisOption.Token("OVERRIDE") override: Boolean?,
    ): CommandRequest
}
