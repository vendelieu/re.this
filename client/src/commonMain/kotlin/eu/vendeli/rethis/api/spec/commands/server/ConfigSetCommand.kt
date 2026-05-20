package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CONFIG SET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ConfigSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RIgnoreSpecAbsence vararg data: KeyValue,
    ): CommandRequest
}
