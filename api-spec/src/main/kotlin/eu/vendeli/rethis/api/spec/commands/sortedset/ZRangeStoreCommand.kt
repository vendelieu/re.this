package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZRangeOption
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZRangeStoreLimit
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "ZRANGESTORE",
    RedisOperation.WRITE,
    [RespCode.INTEGER],
    extensions = [ZRangeOption.Type::class, ZRangeStoreLimit::class],
)
fun interface ZRangeStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        dst: String,
        src: String,
        min: String,
        max: String,
        @RedisOptional rangeType: ZRangeOption.Type?,
        @RedisOptional @RedisOption.Token("REV") rev: Boolean?,
        @RedisOptional limit: ZRangeStoreLimit?
    ): CommandRequest
}
