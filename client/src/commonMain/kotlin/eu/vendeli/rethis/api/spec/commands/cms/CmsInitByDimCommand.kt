package eu.vendeli.rethis.api.spec.commands.cms

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CMS.INITBYDIM", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface CmsInitByDimCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String, width: Long, depth: Long): CommandRequest
}
