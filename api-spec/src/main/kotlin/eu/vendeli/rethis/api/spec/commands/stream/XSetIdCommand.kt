package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XSETID", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface XSetIdCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        lastId: String,
        @RedisOption.Token("ENTRIESADDED") entriesAdded: Long?,
        @RedisOption.Token("MAXDELETEDID") maxDeletedId: String?,
    ): CommandRequest
}
