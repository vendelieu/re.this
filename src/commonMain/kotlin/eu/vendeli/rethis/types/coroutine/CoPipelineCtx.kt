package eu.vendeli.rethis.types.coroutine

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class CoPipelineCtx(
    val pipelinedRequests: MutableList<Any?>,
) : AbstractCoroutineContextElement(CoPipelineCtx),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoPipelineCtx>
}
