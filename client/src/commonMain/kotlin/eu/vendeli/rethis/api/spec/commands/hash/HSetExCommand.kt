package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.hash.HFieldValue
import eu.vendeli.rethis.shared.request.hash.HSetExOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HSETEX", RedisOperation.WRITE, [RespCode.INTEGER, RespCode.BOOLEAN])
fun interface HSetExCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        condition: HSetExOption.Condition?,
        expiration: HSetExOption.Expiration?,
        @RedisOption.Token("FIELDS") @RedisMeta.WithSizeParam("numfields") vararg data: HFieldValue,
    ): CommandRequest
}
