package eu.vendeli.rethis.types.coroutine

import io.ktor.network.sockets.*
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoLocalConn(
    val connection: Connection,
) : AbstractCoroutineContextElement(CoLocalConn),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoLocalConn>
}
