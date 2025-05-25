package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.ClientKillOptions
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT KILL", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [ClientKillOptions::class])
@RedisMeta.CustomCodec(encoder = Any::class) // todo implement
fun interface ClientKillCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisOptional filter: ClientKillOptions?,
    ): CommandRequest<Nothing>
}
