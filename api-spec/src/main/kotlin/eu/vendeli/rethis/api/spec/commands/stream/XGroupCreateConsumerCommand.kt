package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XGROUP CREATECONSUMER", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XGroupCreateConsumerCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        consumer: String
    ): CommandRequest<String>
}
