package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.string.KeyValue
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("MSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [KeyValue::class])
fun interface MSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisKey vararg data: KeyValue,
    ): CommandRequest<String>
}
