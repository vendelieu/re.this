package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.SetExpire
import eu.vendeli.rethis.api.spec.common.request.SetOption
import eu.vendeli.rethis.api.spec.common.request.UpsertMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "SET",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING, RespCode.NULL],
    extensions = [SetOption::class],
)
fun interface SetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey key: String,
        value: String,
        vararg options: SetOption,
    ): CommandRequest<String>
}
