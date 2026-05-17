package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SENTINEL IS-MASTER-DOWN-BY-ADDR", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface SentinelIsMasterDownByAddrCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        ip: String,
        port: Long,
        @RedisOption.Name("current-epoch") currentEpoch: Long,
        runid: String,
    ): CommandRequest
}
