package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.GetExOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "GETEX", 
    RedisOperation.READ,
    [RespCode.SIMPLE_STRING, RespCode.NULL],
    extensions = [GetExOption::class]
)
fun interface GetExCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey key: String,
        option: GetExOption
    ): CommandRequest<String>
}
