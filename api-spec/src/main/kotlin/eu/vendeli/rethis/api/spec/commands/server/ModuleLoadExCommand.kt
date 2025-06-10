package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.server.ModuleOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("MODULE LOADEX", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [ModuleOption::class])
fun interface ModuleLoadExCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(path: String, @RedisOptional vararg options: ModuleOption): CommandRequest
}
