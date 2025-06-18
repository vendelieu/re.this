package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.stream.XId
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XGROUP CREATE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface XGroupCreateCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        group: String,
        idSelector: XId,
        @RedisOption.Token("MKSTREAM") mkstream: Boolean?,
        @RedisOption.Token("ENTRIESREAD") entriesread: Long?
    ): CommandRequest
}
