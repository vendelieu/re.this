package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.stream.XId
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XGROUP SETID", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface XGroupSetIdCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        group: String,
        idSelector: XId,
        @RedisOption.Token("ENTRIESREAD") entriesread: Long?
    ): CommandRequest
}
