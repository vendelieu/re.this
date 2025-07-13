package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.string.KeyValue
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("MSETNX", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface MSetNxCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        vararg data: KeyValue, // todo support getting key type from nested data (not toString() whole data)
    ): CommandRequest
}
