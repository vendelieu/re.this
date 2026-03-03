package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.annotations.ConfigurationDSL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for a connection pool.
 *
 * @property minIdleConnections the minimum number of idle connections, defaults to 10
 * @property maxIdleConnections the maximum number of idle connections, defaults to 100
 * @property maxPendingConnections the maximum number of pending connections, defaults to 1000
 * @property shrinkRatio the ratio of idle connections to be dropped, defaults to 0.5
 * @property maxShrinkSize the maximum number of idle connections to be dropped at once, defaults to 5
 *
 * @property checkInterval the interval at which the pool is checked for idle connections, defaults to 1 second
 * @property connectionAcquirePeriod the period for which connections are acquired, defaults to 3 seconds
 * @property gracefulClosePeriod the period of time for which the pool is closed gracefully, defaults to 30 seconds
 * @property connectionHealthCheck whether to check the health of the connection, defaults to false
 * @property setClientName whether to set the client name, defaults to false
 * @property closeGracefully whether to close the pool gracefully, defaults to false
 */
@ConfigurationDSL
data class PoolConfiguration(
    var minIdleConnections: Int = 10,
    var maxIdleConnections: Int = 100,
    var maxPendingConnections: Int = 1000,
    var shrinkRatio: Double = 0.5,
    var maxShrinkSize: Int = 5,
    var checkInterval: Duration = 1.seconds,
    var gracefulClosePeriod: Duration = 30.seconds,
    var connectionHealthCheck: Boolean = false,
    var setClientName: Boolean = false,
    var closeGracefully: Boolean = false,
)
