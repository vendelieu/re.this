package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.generic.MigrateKey
import eu.vendeli.rethis.shared.request.generic.MigrateOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlin.time.Duration

@RedisCommand(
    "MIGRATE",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING],
)
fun interface MigrateCommand : RedisCommandSpec<String> {
    suspend fun encode(
        host: String,
        port: Long,
        keySelector: MigrateKey,
        destinationDb: Long,
        timeout: Duration,
        @RIgnoreSpecAbsence vararg option: MigrateOption,
    ): CommandRequest
}
