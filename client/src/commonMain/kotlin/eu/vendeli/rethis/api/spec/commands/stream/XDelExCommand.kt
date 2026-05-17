package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.stream.XKeepRefMode
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XDELEX", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface XDelExCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        condition: XKeepRefMode?,
        @RedisOption.Token("IDS") @RedisMeta.WithSizeParam("numids") vararg id: String,
    ): CommandRequest
}
