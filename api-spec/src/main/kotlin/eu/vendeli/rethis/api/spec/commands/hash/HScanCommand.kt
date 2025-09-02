package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.common.PairScanDecoder
import eu.vendeli.rethis.shared.request.hash.HScanOption
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HSCAN", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = PairScanDecoder::class)
fun interface HScanCommand : RedisCommandSpec<ScanResult<Pair<String, String>>> {
    suspend fun encode(
        key: String,
        cursor: Long,
        @RIgnoreSpecAbsence vararg option: HScanOption,
    ): CommandRequest
}
