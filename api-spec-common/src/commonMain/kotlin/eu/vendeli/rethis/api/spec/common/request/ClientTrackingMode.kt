package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import eu.vendeli.rethis.api.spec.common.decoders.ClientTrackingPrefixesDecoder

@RedisOptionContainer
sealed class ClientTrackingMode {
    @RedisOption
    data object OPTIN : ClientTrackingMode()

    @RedisOption
    data object OPTOUT : ClientTrackingMode()

    @RedisOption.Token("BCAST")
    data object BROADCAST : ClientTrackingMode()

    @RedisOption
    data object NOLOOP : ClientTrackingMode()

    @RedisOption.Token("REDIRECT")
    class Redirect(val clientId: Long) : ClientTrackingMode()

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
