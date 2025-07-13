package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.common.LMPopDecoder
import eu.vendeli.rethis.api.spec.common.response.MPopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZPOPMAX", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = LMPopDecoder::class)
fun interface ZPopMaxCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        key: String,
        count: Long?
    ): CommandRequest
}
