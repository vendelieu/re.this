package eu.vendeli.rethis.api.spec.commands.hyperloglog

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PFCOUNT", RedisOperation.READ, [RespCode.INTEGER])
fun interface PfCountCommand : RedisCommandSpec<Long> {
    suspend fun encode(@RedisKey vararg key: String): CommandRequest<List<String>>
}
