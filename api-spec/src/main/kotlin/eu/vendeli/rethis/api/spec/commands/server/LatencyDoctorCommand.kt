package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LATENCY DOCTOR", RedisOperation.READ, [RespCode.BULK, RespCode.VERBATIM_STRING])
fun interface LatencyDoctorCommand : RedisCommandSpec<String> {
    suspend fun encode(): CommandRequest
}
