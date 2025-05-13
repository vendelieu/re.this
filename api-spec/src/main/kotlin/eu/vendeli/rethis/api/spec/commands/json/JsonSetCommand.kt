package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.UpsertMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.SET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [UpsertMode::class])
fun interface JsonSetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey key: String,
        value: String,
        @RedisOptional path: String?,
        @RedisOptional upsertMode: UpsertMode?
    ): CommandRequest<String>
}
