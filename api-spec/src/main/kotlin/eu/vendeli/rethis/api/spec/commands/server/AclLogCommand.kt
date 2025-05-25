package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "ACL LOG",
    RedisOperation.READ,
    [RespCode.SIMPLE_STRING, RespCode.ARRAY],
) // todo handle string ok as empty string
fun interface AclLogCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisOptional count: Long?,
        @RedisOptional @RedisOption.Token("RESET") reset: Boolean?,
    ): CommandRequest<Nothing>
}
