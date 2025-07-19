package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.common.StringScanDecoder
import eu.vendeli.rethis.api.spec.common.request.set.SScanOption
import eu.vendeli.rethis.api.spec.common.response.common.ScanResult
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("SSCAN", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = StringScanDecoder::class)
fun interface SScanCommand : RedisCommandSpec<ScanResult<String>> {
    suspend fun encode(
        key: String,
        cursor: Long,
        @RIgnoreSpecAbsence vararg option: SScanOption,
    ): CommandRequest
}
