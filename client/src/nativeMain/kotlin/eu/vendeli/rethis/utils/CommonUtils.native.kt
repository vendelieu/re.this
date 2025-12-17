package eu.vendeli.rethis.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking

@OptIn(DelicateCoroutinesApi::class)
actual val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher
    get() = newFixedThreadPoolContext(nThreads = 200, name = "IO")

actual fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T) = runBlocking(block = block)
