package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("MEMORY DOCTOR", RedisOperation.READ, [RespCode.BULK, RespCode.VERBATIM_STRING])
fun interface MemoryDoctorCommand : RedisCommandSpec<String> {
    suspend fun encode(): CommandRequest<Nothing>
}
