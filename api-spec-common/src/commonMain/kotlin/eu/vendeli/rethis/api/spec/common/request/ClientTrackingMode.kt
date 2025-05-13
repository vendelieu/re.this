package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.decoders.ClientTrackingPrefixesDecoder

sealed class ClientTrackingMode {
    @RedisOption
    @RedisMeta.OrderPriority(4)
    class OPTIN : ClientTrackingMode()

    @RedisOption
    @RedisMeta.OrderPriority(5)
    class OPTOUT : ClientTrackingMode()

    @RedisMeta.OrderPriority(3)
    @RedisOption.Name("BCAST")
    class BROADCAST : ClientTrackingMode()

    @RedisOption
    @RedisMeta.OrderPriority(6)
    class NOLOOP : ClientTrackingMode()

    @RedisMeta.OrderPriority(1)
    @RedisOption.Name("REDIRECT")
    class Redirect(val clientId: Long) : ClientTrackingMode()

    @RedisMeta.OrderPriority(2)
    @RedisMeta.CustomCodec(decoder = ClientTrackingPrefixesDecoder::class)
    @RedisMeta.Ignore
    @RedisOption.Name("PREFIX")
    class Prefixes(vararg prefix: String) : ClientTrackingMode()
}

@RedisOptionContainer
sealed class ClientStandby : ClientTrackingMode() {
    @RedisOption
    data object ON : ClientStandby()

    @RedisOption
    data object OFF : ClientStandby()
}
