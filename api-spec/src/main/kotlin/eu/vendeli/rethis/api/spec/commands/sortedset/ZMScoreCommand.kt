package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZMSCORE", RedisOperation.READ, [RespCode.ARRAY])
fun interface ZMScoreCommand : RedisCommandSpec<List<Double?>> {
    suspend fun encode(
        @RedisKey key: String,
        vararg members: String
    ): CommandRequest<String>
}
