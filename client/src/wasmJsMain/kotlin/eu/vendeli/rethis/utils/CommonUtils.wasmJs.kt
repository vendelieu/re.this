package eu.vendeli.rethis.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher
    get() = Dispatchers.Unconfined

actual fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T =
    throw BlockingUnsupportedException()
