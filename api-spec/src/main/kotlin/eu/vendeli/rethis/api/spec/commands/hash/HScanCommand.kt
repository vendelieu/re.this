package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.common.PairScanDecoder
import eu.vendeli.rethis.api.spec.common.request.hash.HScanOption
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("HSCAN", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.IgnoreCheck([ValidityCheck.RESPONSE])
@RedisMeta.CustomCodec(decoder = PairScanDecoder::class)
fun interface HScanCommand : RedisCommandSpec<ScanResult<Pair<String, String>>> {
    suspend fun encode(
        key: String,
        cursor: Long,
        @RIgnoreSpecAbsence vararg option: HScanOption,
    ): CommandRequest
}
