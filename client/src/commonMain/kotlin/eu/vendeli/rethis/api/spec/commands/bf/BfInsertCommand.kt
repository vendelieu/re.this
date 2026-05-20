package eu.vendeli.rethis.api.spec.commands.bf

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.request.bf.BfInsertOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BF.INSERT", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface BfInsertCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        @RIgnoreSpecAbsence vararg options: BfInsertOption,
        @RedisOption.Token("ITEMS") item: List<String>,
    ): CommandRequest
}
