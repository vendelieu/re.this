package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LATENCY DOCTOR", RedisOperation.READ, [RespCode.BULK, RespCode.VERBATIM_STRING])
fun interface LatencyDoctorCommand : RedisCommandSpec<String> {
    suspend fun encode(): CommandRequest<Nothing>
}
