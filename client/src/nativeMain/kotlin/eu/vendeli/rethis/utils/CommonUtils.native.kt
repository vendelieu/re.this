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

/**
 * Created once and shared by every client in the process, so the thread limit holds no matter how many clients
 * and pools are created. It is intentionally never closed: no single client owns it.
 */
@OptIn(DelicateCoroutinesApi::class)
private val ioDispatcher: CoroutineDispatcher by lazy {
    newFixedThreadPoolContext(nThreads = parallelismLimit, name = "IO")
}

actual val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher
    get() = ioDispatcher

actual fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T) = runBlocking(block = block)
