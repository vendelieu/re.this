package eu.vendeli.rethis.api.spec.commands.vector

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("VEMB", RedisOperation.READ, [RespCode.ARRAY, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface VEmbCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        element: String,
        @RedisOption.Token("RAW") raw: Boolean?,
    ): CommandRequest
}
