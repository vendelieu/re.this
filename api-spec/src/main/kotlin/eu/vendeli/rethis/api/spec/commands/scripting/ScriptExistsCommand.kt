package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.common.ArrayIntBooleanDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SCRIPT EXISTS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = ArrayIntBooleanDecoder::class)
fun interface ScriptExistsCommand : RedisCommandSpec<List<Boolean>> {
    suspend fun encode(vararg sha1: String): CommandRequest
}
