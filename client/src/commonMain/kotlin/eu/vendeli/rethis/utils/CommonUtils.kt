@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.configuration.RetryConfiguration
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

expect val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher

expect fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T

internal suspend inline fun <reified T : CoroutineContext.Element> takeFromCoCtx(element: CoroutineContext.Key<T>): T? =
    currentCoroutineContext()[element]

internal suspend inline fun <T> withRetry(
    cfg: RetryConfiguration,
    block: suspend () -> T,
): T {
    var currentDelay = cfg.initialDelay.inWholeMilliseconds
    repeat(cfg.times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            // todo LOG
        }
        delay(currentDelay)
        currentDelay = (currentDelay * cfg.factor).toLong().coerceAtMost(cfg.maxDelay.inWholeMilliseconds)
    }
    return block()
}
