package eu.vendeli.rethis.api.spec.commands.transaction

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("EXEC", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL])
fun interface ExecCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(): CommandRequest<Nothing>
}
