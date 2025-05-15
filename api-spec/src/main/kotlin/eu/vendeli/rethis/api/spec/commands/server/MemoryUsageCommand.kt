package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("MEMORY USAGE", RedisOperation.READ, [RespCode.INTEGER, RespCode.NULL])
fun interface MemoryUsageCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisOptional @RedisOption.Token("SAMPLES") count: Long?
    ): CommandRequest<String>
}
