package eu.vendeli.rethis.api.spec.commands.vector

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.request.vector.VSimOption
import eu.vendeli.rethis.shared.request.vector.VSimSource
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("VSIM", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface VSimCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        source: VSimSource,
        @RIgnoreSpecAbsence vararg options: VSimOption,
    ): CommandRequest
}
