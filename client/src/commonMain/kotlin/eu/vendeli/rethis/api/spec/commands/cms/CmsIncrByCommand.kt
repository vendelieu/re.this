package eu.vendeli.rethis.api.spec.commands.cms

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.response.cms.CmsIncrement
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CMS.INCRBY", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface CmsIncrByCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(key: String, vararg items: CmsIncrement): CommandRequest
}
