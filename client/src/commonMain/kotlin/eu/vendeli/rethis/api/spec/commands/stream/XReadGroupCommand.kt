package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.aggregate.XReadGroupDecoder
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.types.stream.XReadGroupResponse

@RedisCommand(
    "XREADGROUP",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
)
@RedisMeta.CustomCodec(decoder = XReadGroupDecoder::class)
fun interface XReadGroupCommand : RedisCommandSpec<List<XReadGroupResponse>> {
    suspend fun encode(
        @RedisOption.Token("GROUP") group: String,
        consumer: String,
        streams: XReadGroupKeyIds,
        @RIgnoreSpecAbsence vararg option: XReadGroupOption,
    ): CommandRequest
}
