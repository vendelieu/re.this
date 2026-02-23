package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FLUSHDB", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FlushDbCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(flushType: FlushType?): CommandRequest
}
