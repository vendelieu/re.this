package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.utils.JSON_DEFAULT_PATH

@RedisCommand("JSON.RESP", RedisOperation.READ, [RespCode.ARRAY])
fun interface JsonRespCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        @RedisMeta.Default("\"$JSON_DEFAULT_PATH\"") path: String?,
    ): CommandRequest
}
