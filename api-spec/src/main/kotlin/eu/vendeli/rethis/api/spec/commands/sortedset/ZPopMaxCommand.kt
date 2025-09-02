package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.common.LMPopDecoder
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZPOPMAX", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = LMPopDecoder::class)
fun interface ZPopMaxCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        key: String,
        count: Long?
    ): CommandRequest
}
