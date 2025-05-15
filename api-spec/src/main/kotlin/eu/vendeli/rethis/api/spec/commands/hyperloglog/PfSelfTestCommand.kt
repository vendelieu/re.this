package eu.vendeli.rethis.api.spec.commands.hyperloglog

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("PFSELFTEST", RedisOperation.READ, [RespCode.SIMPLE_STRING])
fun interface PfSelfTestCommand : RedisCommandSpec<RType> {
    suspend fun encode(): CommandRequest<Nothing>
}
