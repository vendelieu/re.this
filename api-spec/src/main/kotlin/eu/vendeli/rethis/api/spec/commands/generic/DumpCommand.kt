package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.generic.DumpDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("DUMP", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = DumpDecoder::class)
fun interface DumpCommand : RedisCommandSpec<ByteArray> {
    suspend fun encode(key: String): CommandRequest
}
