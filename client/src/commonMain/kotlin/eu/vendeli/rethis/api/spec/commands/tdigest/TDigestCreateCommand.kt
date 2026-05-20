package eu.vendeli.rethis.api.spec.commands.tdigest

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TDIGEST.CREATE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface TDigestCreateCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("COMPRESSION") compression: Long?,
    ): CommandRequest
}
