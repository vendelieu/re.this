package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.common.StringScanDecoder
import eu.vendeli.rethis.shared.request.generic.ScanOption
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SCAN", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = StringScanDecoder::class)
fun interface ScanCommand : RedisCommandSpec<ScanResult<String>> {
    suspend fun encode(
        cursor: Long,
        @RIgnoreSpecAbsence vararg option: ScanOption,
    ): CommandRequest
}
