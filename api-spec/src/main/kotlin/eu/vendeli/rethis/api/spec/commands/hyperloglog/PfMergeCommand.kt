package eu.vendeli.rethis.api.spec.commands.hyperloglog

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("PFMERGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface PfMergeCommand : RedisCommandSpec<String> {
    suspend fun encode(
        destkey: String,
        vararg sourcekey: String
    ): CommandRequest
}
