package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.shared.request.string.MSetExOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("MSETEX", RedisOperation.WRITE, [RespCode.INTEGER, RespCode.SIMPLE_STRING])
fun interface MSetExCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisMeta.WithSizeParam("numkeys") vararg data: KeyValue,
        condition: MSetExOption.Condition?,
        expiration: MSetExOption.Expiration?,
    ): CommandRequest
}
