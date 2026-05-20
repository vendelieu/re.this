package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.request.search.FtProfileQueryType
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.PROFILE", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface FtProfileCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        index: String,
        @RedisOption.Name("querytype") queryType: FtProfileQueryType,
        @RedisOption.Token("LIMITED") limited: Boolean?,
        @RedisOption.Token("QUERY") query: String,
        @RIgnoreSpecAbsence vararg args: String,
    ): CommandRequest
}
