package eu.vendeli.rethis.api.spec.commands.tdigest

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.response.tdigest.TDigestValue
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TDIGEST.ADD", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface TDigestAddCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String, vararg values: TDigestValue): CommandRequest
}
