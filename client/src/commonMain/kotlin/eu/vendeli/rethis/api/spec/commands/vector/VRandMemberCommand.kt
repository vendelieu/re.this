package eu.vendeli.rethis.api.spec.commands.vector

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("VRANDMEMBER", RedisOperation.READ, [RespCode.BULK, RespCode.ARRAY, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface VRandMemberCommand : RedisCommandSpec<RType> {
    suspend fun encode(key: String, count: Long?): CommandRequest
}
