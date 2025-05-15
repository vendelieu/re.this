package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XPendingOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("XPENDING", RedisOperation.READ, [RespCode.ARRAY], extensions = [XPendingOption::class])
fun interface XPendingCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        @RedisOptional option: XPendingOption?
    ): CommandRequest<String>
}
