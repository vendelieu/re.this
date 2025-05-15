package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XId
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XGROUP CREATE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [XId::class])
fun interface XGroupCreateCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        id: XId,
        @RedisOptional @RedisOption.Token("MKSTREAM") mkstream: Boolean?,
        @RedisOptional entriesRead: Long?
    ): CommandRequest<String>
}
