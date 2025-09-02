package eu.vendeli.rethis.types.coroutine

import eu.vendeli.rethis.types.common.RConnection
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class CoLocalConn(
    val connection: RConnection,
    val isTx: Boolean = true
) : AbstractCoroutineContextElement(CoLocalConn),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoLocalConn>
}
