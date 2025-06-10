package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT KILL", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
@RedisMeta.CustomCodec(encoder = Any::class) // todo addr:port
fun interface ClientKillStringCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        addr: String,
        port: Long
    ): CommandRequest
}
