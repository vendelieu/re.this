package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.stream.XId
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XGROUP SETID", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface XGroupSetIdCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        group: String,
        idSelector: XId,
        @RedisOption.Token("ENTRIESREAD") entriesread: Long?
    ): CommandRequest
}
