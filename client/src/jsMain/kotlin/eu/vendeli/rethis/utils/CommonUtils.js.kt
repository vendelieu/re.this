package eu.vendeli.rethis.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

actual val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher
    get() = Unconfined

@OptIn(DelicateCoroutinesApi::class)
actual fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T = GlobalScope.async { block(this) }.asDynamic()
