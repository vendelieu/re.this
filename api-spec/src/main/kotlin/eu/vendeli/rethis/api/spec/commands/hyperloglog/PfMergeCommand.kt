package eu.vendeli.rethis.api.spec.commands.hyperloglog

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PFMERGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface PfMergeCommand : RedisCommandSpec<String> {
    suspend fun encode(
        destkey: String,
        @RedisOptional vararg sourcekey: String
    ): CommandRequest
}
