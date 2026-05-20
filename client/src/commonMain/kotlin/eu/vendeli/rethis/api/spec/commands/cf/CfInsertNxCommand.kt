package eu.vendeli.rethis.api.spec.commands.cf

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CF.INSERTNX", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface CfInsertNxCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("CAPACITY") capacity: Long?,
        @RedisOption.Token("NOCREATE") nocreate: Boolean?,
        @RedisOption.Token("ITEMS") item: List<String>,
    ): CommandRequest
}
