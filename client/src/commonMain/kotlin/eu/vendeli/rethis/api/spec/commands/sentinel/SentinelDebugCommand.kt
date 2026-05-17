package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.request.sentinel.SentinelDebugOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisCommand("SENTINEL DEBUG", RedisOperation.WRITE, [])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface SentinelDebugCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        @RIgnoreSpecAbsence vararg options: SentinelDebugOption,
    ): CommandRequest
}
