package eu.vendeli.rethis.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

actual val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher
    get() = IO

actual fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T) = runBlocking(block = block)
