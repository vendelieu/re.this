package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.annotations.ReThisExperimental
import eu.vendeli.rethis.types.common.ReThisReentrantLock
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Structured helper: guarantees unlock in finally, safe for coroutine cancellation.
suspend inline fun <T> ReDistributedLock.withLock(
    leaseTime: Duration = Duration.ZERO,
    crossinline block: suspend () -> T,
): T {
    lock(leaseTime)
    try {
        return block()
    } finally {
        withContext(NonCancellable) { unlock() }
    }
}

@ReThisExperimental
suspend fun ReThis.reDistributedLock(
    key: String,
    waitTime: Duration = 50.milliseconds,
    leaseTime: Duration = 30.seconds,
): ReDistributedLock {
    val referenceJob = currentCoroutineContext()[Job] ?: error("No Job in context")
    return ReThisReentrantLock(
        client = this,
        key = key,
        referenceJob = referenceJob,
        defaultLeaseMs = leaseTime.inWholeMilliseconds,
        backoffBaseMs = waitTime.inWholeMilliseconds,
    )
}
