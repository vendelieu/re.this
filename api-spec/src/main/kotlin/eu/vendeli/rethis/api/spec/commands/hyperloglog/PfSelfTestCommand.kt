package eu.vendeli.rethis.api.spec.commands.hyperloglog

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PFSELFTEST", RedisOperation.READ, [RespCode.SIMPLE_STRING])
fun interface PfSelfTestCommand : RedisCommandSpec<RType> {
    suspend fun encode(): CommandRequest<Nothing>
}
