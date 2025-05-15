package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.decoders.ClientTrackingPrefixesDecoder

@RedisOptionContainer
sealed class ClientTrackingMode {
    @RedisOption
    @RedisMeta.OrderPriority(4)
    data object OPTIN : ClientTrackingMode()

    @RedisOption
    @RedisMeta.OrderPriority(5)
    data object OPTOUT : ClientTrackingMode()

    @RedisMeta.OrderPriority(3)
    @RedisOption.Token("BCAST")
    data object BROADCAST : ClientTrackingMode()

    @RedisOption
    @RedisMeta.OrderPriority(6)
    data object NOLOOP : ClientTrackingMode()

    @RedisMeta.OrderPriority(1)
    @RedisOption.Token("REDIRECT")
    class Redirect(val clientId: Long) : ClientTrackingMode()

    @RedisMeta.OrderPriority(2)
    @RedisMeta.CustomCodec(decoder = ClientTrackingPrefixesDecoder::class)
    @RedisOption.Token("PREFIX")
    class Prefixes(vararg val prefix: String) : ClientTrackingMode()
}

@RedisOptionContainer
sealed class ClientStandby : ClientTrackingMode() {
    @RedisOption
    data object ON : ClientStandby()

    @RedisOption
    data object OFF : ClientStandby()
}
