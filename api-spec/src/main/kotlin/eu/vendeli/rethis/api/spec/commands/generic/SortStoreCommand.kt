package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.generic.SortOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "SORT",
    RedisOperation.WRITE,
    [RespCode.INTEGER],
)
fun interface SortStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("STORE") @RedisOption.Name("destination") storeDestination: String,
        @RIgnoreSpecAbsence vararg option: SortOption,
    ): CommandRequest
}
