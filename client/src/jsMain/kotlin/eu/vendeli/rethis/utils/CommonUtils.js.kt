package eu.vendeli.rethis.utils

import kotlinx.coroutines.*

actual val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher
    get() = Unconfined

@OptIn(DelicateCoroutinesApi::class)
actual fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T = GlobalScope.async { block(this) }.asDynamic()
