package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.connection.AclLogDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "ACL LOG",
    RedisOperation.READ,
    [RespCode.SIMPLE_STRING, RespCode.ARRAY],
)
@RedisMeta.CustomCodec(decoder = AclLogDecoder::class)
fun interface AclLogCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        count: Long?,
        @RedisOption.Token("RESET") reset: Boolean?,
    ): CommandRequest
}
