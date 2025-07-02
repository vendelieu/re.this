package eu.vendeli.rethis.types.coroutine

import kotlinx.io.Buffer
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class CoPipelineCtx(
    val pipelined: MutableList<Buffer>,
) : AbstractCoroutineContextElement(CoPipelineCtx),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoPipelineCtx>
}
