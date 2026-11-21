package eu.vendeli.rethis.shared.request.timeseries

import eu.vendeli.rethis.shared.annotations.RedisOption

/**
 * Which timestamp of its bucket an aggregated sample reports (`BUCKETTIMESTAMP`).
 */
@RedisOption.Token("BUCKETTIMESTAMP")
sealed class TsBucketTimestamp {
    /** Start of the bucket (the default). */
    @RedisOption.Token("-")
    data object Low : TsBucketTimestamp()

    /** End of the bucket. */
    @RedisOption.Token("+")
    data object High : TsBucketTimestamp()

    /** Middle of the bucket. */
    @RedisOption.Token("~")
    data object Mid : TsBucketTimestamp()
}
