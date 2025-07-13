package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.response.ZMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZRANDMEMBER", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ResponseDecoder::class) // todo add
fun interface ZRandMemberWithScoresCommand : RedisCommandSpec<List<List<ZMember>>> {
    suspend fun encode(
        key: String,
        count: Long,
        @RedisOption.Token("WITHSCORES") @RedisOption.Name("withscores") withScores: Boolean?
    ): CommandRequest
}
