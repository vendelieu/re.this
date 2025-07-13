package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.common.StringScanDecoder
import eu.vendeli.rethis.api.spec.common.request.generic.ScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SCAN", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = StringScanDecoder::class)
fun interface ScanCommand : RedisCommandSpec<ScanResult<String>> {
    suspend fun encode(
        cursor: Long,
        @RIgnoreSpecAbsence vararg option: ScanOption,
    ): CommandRequest
}
