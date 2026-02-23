package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("MEMORY USAGE", RedisOperation.READ, [RespCode.INTEGER, RespCode.NULL])
fun interface MemoryUsageCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("SAMPLES") count: Long?
    ): CommandRequest
}
