package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZRANK", RedisOperation.READ, [RespCode.INTEGER, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = ResponseDecoder::class) // todo add
fun interface ZRankCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        member: String
    ): CommandRequest
}
