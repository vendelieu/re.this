package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("XINFO STREAM", RedisOperation.READ, [RespCode.MAP, RespCode.ARRAY], extensions = [XOption.Limit::class])
fun interface XInfoStreamCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisOptional full: Boolean?,
        @RedisOptional limit: XOption.Limit?
    ): CommandRequest<String>
}
