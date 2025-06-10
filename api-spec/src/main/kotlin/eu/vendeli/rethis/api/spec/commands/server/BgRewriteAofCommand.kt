package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BGREWRITEAOF", RedisOperation.WRITE, [RespCode.SIMPLE_STRING, RespCode.BULK])
fun interface BgRewriteAofCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(): CommandRequest
}
