package eu.vendeli.rethis.types.coroutine

import eu.vendeli.rethis.types.common.Argument
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class CoPipelineCtx(
    val pipelinedRequests: MutableList<List<Argument>>,
) : AbstractCoroutineContextElement(CoPipelineCtx),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoPipelineCtx>
}
