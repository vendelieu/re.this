package eu.vendeli.rethis.api.spec.commands.cf

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CF.COUNT", RedisOperation.READ, [RespCode.INTEGER])
fun interface CfCountCommand : RedisCommandSpec<Long> {
    suspend fun encode(key: String, item: String): CommandRequest
}
