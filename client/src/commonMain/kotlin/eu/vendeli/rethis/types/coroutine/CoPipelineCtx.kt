package eu.vendeli.rethis.types.coroutine

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class CoPipelineCtx(
    val pipelined: MutableList<CommandRequest>,
) : AbstractCoroutineContextElement(CoPipelineCtx),
    CoroutineContext.Element {
    override val key = Key

    companion object Key : CoroutineContext.Key<CoPipelineCtx>
}
