package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.generic.DumpDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FUNCTION DUMP", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = DumpDecoder::class)
fun interface FunctionDumpCommand : RedisCommandSpec<ByteArray> {
    suspend fun encode(): CommandRequest
}
