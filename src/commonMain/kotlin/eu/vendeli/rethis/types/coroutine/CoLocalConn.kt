package eu.vendeli.rethis.types.coroutine

import eu.vendeli.rethis.types.core.RConnection
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoLocalConn(
    val connection: RConnection,
) : AbstractCoroutineContextElement(CoLocalConn),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoLocalConn>
}
