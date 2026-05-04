package eu.vendeli.rethis.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform.getAvailableProcessors

/**
 * As in [Dispatchers.IO], it defaults to the limit of 64 threads or the number of cores (whichever is larger).
 */
@OptIn(ExperimentalNativeApi::class)
private val parallelismLimit = maxOf(getAvailableProcessors(), 64)

@OptIn(DelicateCoroutinesApi::class)
actual val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher
    get() = newFixedThreadPoolContext(nThreads = parallelismLimit, name = "IO")

actual fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T) = runBlocking(block = block)
