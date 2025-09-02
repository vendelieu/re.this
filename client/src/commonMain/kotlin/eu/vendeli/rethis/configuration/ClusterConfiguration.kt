package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.types.common.RespVer
import kotlin.time.Duration.Companion.seconds

class ClusterConfiguration(
    protocol: RespVer,
) : ReThisConfiguration(protocol) {
    var periodicRefresh: Boolean = false
    var periodicRefreshInterval = 30.seconds
    var movedBackoffPeriod = 5.seconds
}
