package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.MigrateOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlin.time.Duration

@RedisCommand("MIGRATE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [MigrateOption::class])
fun interface MigrateCommand : RedisCommandSpec<String> {
    suspend fun encode(
        host: String,
        port: Long,
        @RedisKey @RedisOptional keys: String?,
        destinationDb: Long,
        timeout: Duration,
        vararg option: MigrateOption
    ): CommandRequest<String>
}
