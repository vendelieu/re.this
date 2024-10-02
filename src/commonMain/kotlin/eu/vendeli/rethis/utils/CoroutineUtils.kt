package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal suspend inline fun ReThis.coLaunch(
    context: CoroutineContext? = null,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend CoroutineScope.() -> Unit,
): Job = coroutineScope {
    launch(
        (context ?: currentCoroutineContext()) + CoroutineName("ReThis") + Job(rootJob) + cfg.dispatcher,
        start,
        block,
    )
}
