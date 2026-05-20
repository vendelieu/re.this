package eu.vendeli.rethis.api.spec.commands.cms

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CMS.QUERY", RedisOperation.READ, [RespCode.ARRAY])
fun interface CmsQueryCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(key: String, vararg item: String): CommandRequest
}
