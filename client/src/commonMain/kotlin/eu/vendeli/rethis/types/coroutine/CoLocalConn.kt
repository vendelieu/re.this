package eu.vendeli.rethis.types.coroutine

import eu.vendeli.rethis.types.common.RConnection
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Pins the coroutine to one [connection].
 *
 * [isTx] `true` means the commands are queued inside `MULTI`: their replies are replaced by an empty buffer.
 * `false` is a plain pinned connection that returns the real replies.
 */
internal class CoLocalConn(
    val connection: RConnection,
    val isTx: Boolean,
) : AbstractCoroutineContextElement(CoLocalConn),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoLocalConn>
}
