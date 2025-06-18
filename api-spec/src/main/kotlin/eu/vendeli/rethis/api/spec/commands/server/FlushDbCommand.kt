package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.common.FlushType
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("FLUSHDB", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FlushDbCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(flushType: FlushType?): CommandRequest
}
