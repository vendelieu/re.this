package eu.vendeli.rethis.configuration

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for the retry mechanism.
 *
 * @property times the number of times to retry the action, defaults to 3
 * @property initialDelay the initial delay between retries, defaults to 100ms
 * @property maxDelay the maximum delay between retries, defaults to 1s
 * @property factor the factor to multiply the delay by on each retry, defaults to 2.0
 */
data class RetryConfiguration(
    var times: Int = 3,
    var initialDelay: Duration = 100.milliseconds,
    var maxDelay: Duration = 1.seconds,
    var factor: Double = 2.0,
)
