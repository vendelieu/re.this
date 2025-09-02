package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.common.ArrayIntBooleanDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SCRIPT EXISTS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ArrayIntBooleanDecoder::class)
fun interface ScriptExistsCommand : RedisCommandSpec<List<Boolean>> {
    suspend fun encode(vararg sha1: String): CommandRequest
}
