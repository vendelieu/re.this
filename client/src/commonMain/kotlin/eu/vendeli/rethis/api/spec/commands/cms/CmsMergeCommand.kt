package eu.vendeli.rethis.api.spec.commands.cms

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CMS.MERGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface CmsMergeCommand : RedisCommandSpec<String> {
    suspend fun encode(
        destination: String,
        @RedisMeta.WithSizeParam("numKeys") vararg source: String,
        @RedisOption.Token("WEIGHTS") weight: List<Double>,
    ): CommandRequest
}
