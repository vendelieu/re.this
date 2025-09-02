package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.sortedset.ZRangeOption
import eu.vendeli.rethis.shared.request.sortedset.ZRangeStoreLimit
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "ZRANGESTORE",
    RedisOperation.WRITE,
    [RespCode.INTEGER],
)
fun interface ZRangeStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        dst: String,
        src: String,
        min: String,
        max: String,
        @RedisOption.Name("sortby") sortBy: ZRangeOption.Type?,
        @RedisOption.Token("REV") rev: Boolean?,
        limit: ZRangeStoreLimit?
    ): CommandRequest
}
