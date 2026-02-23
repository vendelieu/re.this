package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("JSON.RESP", RedisOperation.READ, [RespCode.ARRAY])
fun interface JsonRespCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        path: String?
    ): CommandRequest
}
