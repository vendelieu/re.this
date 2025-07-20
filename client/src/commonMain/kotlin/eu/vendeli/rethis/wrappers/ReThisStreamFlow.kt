package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.api.spec.common.request.stream.XReadGroupOption
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.command.stream.xReadGroup
import eu.vendeli.rethis.utils.IO_OR_UNCONFINED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Consume a Redis Stream as a cold Flow.
 *
 * @param key        the stream key
 * @param group      the consumer-group name (must already exist)
 * @param consumer   the consumer name
 * @param batchSize  max messages to fetch per XREADGROUP
 * @param block    how long to block when no messages are available
 * @param acknowledge  whether to ACK messages as they are consumed
 */
@Suppress("FunctionName")
fun ReThis.StreamFlow(
    key: String,
    group: String,
    consumer: String,
    batchSize: Long = 10,
    block: Duration = Duration.ZERO,
    acknowledge: Boolean = true,
): Flow<Map<String, RType>> = flow {
    val options = buildList {
        if (block.isPositive()) add(XReadGroupOption.Block(block))
        add(XReadGroupOption.Count(batchSize))
        if (!acknowledge) add(XReadGroupOption.NoAck)
    }.toTypedArray()

    // we use ">" so we only get new messages after the last delivered ID for this consumer
    scope.launch(Dispatchers.IO_OR_UNCONFINED) {
        while (isActive) {
            // XREADGROUP GROUP <group> <consumer> BLOCK <blockMs> COUNT <batchSize> STREAMS <key> >
            val msgs = xReadGroup(group, consumer, XReadGroupKeyIds(listOf(key), listOf(">")), *options).orEmpty()

            emit(msgs)
        }
    }.join()
}
