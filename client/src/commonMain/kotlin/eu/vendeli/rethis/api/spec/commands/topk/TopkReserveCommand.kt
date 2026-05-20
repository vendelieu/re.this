package eu.vendeli.rethis.api.spec.commands.topk

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.topk.TopkReserveParams
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TOPK.RESERVE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface TopkReserveCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        topk: Long,
        params: TopkReserveParams?,
    ): CommandRequest
}
