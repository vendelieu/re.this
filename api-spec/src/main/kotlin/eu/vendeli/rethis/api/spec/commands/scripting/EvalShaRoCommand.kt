package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisCommand("EVALSHA_RO", RedisOperation.READ, [])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface EvalShaRoCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        sha1: String,
        @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        arg: List<String>
    ): CommandRequest
}
