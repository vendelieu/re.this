package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SENTINEL SET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface SentinelSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        name: String,
        vararg optionValue: FieldValue,
    ): CommandRequest
}
