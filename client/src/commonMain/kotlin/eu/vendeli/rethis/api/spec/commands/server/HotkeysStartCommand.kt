package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.server.HotkeysStartOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HOTKEYS START", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface HotkeysStartCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RIgnoreSpecAbsence vararg options: HotkeysStartOption,
    ): CommandRequest
}
