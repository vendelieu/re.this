package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XCFGSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface XCfgSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("IDMP-DURATION") idmpDuration: Long?,
        @RedisOption.Token("IDMP-MAXSIZE") idmpMaxSize: Long?,
    ): CommandRequest
}
