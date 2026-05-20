package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.SUGGET", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface FtSugGetCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        prefix: String,
        @RedisOption.Token("FUZZY") fuzzy: Boolean?,
        @RedisOption.Token("WITHSCORES") withScores: Boolean?,
        @RedisOption.Token("WITHPAYLOADS") withPayloads: Boolean?,
        @RedisOption.Token("MAX") max: Long?,
    ): CommandRequest
}
