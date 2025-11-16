package eu.vendeli.rethis.types.interfaces

import kotlin.time.Duration

/**
 * Distributed reentrant lock interface.
 */
interface ReDistributedLock {
    suspend fun tryLock(
        waitTime: Duration = Duration.ZERO,
        leaseTime: Duration = Duration.ZERO,
    ): Boolean

    suspend fun lock(leaseTime: Duration = Duration.ZERO)
    suspend fun unlock(): Boolean
}
