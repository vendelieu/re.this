package eu.vendeli.rethis.api.spec.commands.cms

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CMS.INITBYPROB", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface CmsInitByProbCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String, error: Double, probability: Double): CommandRequest
}
