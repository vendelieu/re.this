package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisCommand("FCALL_RO", RedisOperation.READ, [])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface FcallRoCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        function: String,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        arg: List<String>
    ): CommandRequest
}
