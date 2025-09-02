package eu.vendeli.rethis.shared.request.connection

import eu.vendeli.rethis.shared.annotations.RedisOption


sealed class ClientTrackingMode {
    data object OPTIN : ClientTrackingMode()

    data object OPTOUT : ClientTrackingMode()

    @RedisOption.Token("BCAST")
    data object BROADCAST : ClientTrackingMode()

    data object NOLOOP : ClientTrackingMode()

    @RedisOption.Token("REDIRECT")
    class Redirect(val clientId: Long) : ClientTrackingMode()

    class Prefixes(@RedisOption.Token("PREFIX") vararg val prefix: String) : ClientTrackingMode()
}


sealed class ClientStandby : ClientTrackingMode() {
    data object ON : ClientStandby()

    data object OFF : ClientStandby()
}
