package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.*

@RedisCommand(
    "ACL LOG",
    RedisOperation.READ,
    [RespCode.SIMPLE_STRING, RespCode.ARRAY],
)
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface AclLogCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        count: Long?,
        @RedisOption.Token("RESET") reset: Boolean?,
    ): CommandRequest
}
