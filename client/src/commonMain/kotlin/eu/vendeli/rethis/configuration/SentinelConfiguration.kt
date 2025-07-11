package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.types.common.RespVer
import kotlin.time.Duration.Companion.seconds

class SentinelConfiguration(
    protocol: RespVer,
) : ReThisConfiguration(protocol) {
    var periodicRefresh: Boolean = false
    var periodicRefreshInterval = 30.seconds
}
