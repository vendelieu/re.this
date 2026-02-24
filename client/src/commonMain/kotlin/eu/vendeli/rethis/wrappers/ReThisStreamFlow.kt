package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.stream.xAck
import eu.vendeli.rethis.command.stream.xReadGroup
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.stream.XReadGroupResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

/**
 * A message from a Redis Stream.
 *
 * @property id The message ID.
 * @property stream The stream key.
 * @property data The message fields and values.
 */
data class ReThisStreamMessage(
    val id: String,
    val stream: String,
    val data: Map<String, RType>,
)

/**
 * Consume a Redis Stream as a cold Flow.
 *
 * @param key        the stream key
 * @param group      the consumer-group name (must already exist)
 * @param consumer   the consumer name
 * @param batchSize  max messages to fetch per XREADGROUP
 * @param block      how long to block when no messages are available
 * @param acknowledge whether to ACK messages as they are consumed (at-least-once)
 */
@Suppress("FunctionName")
fun ReThis.StreamFlow(
    key: String,
    group: String,
    consumer: String,
    batchSize: Long = 10,
    block: Duration = Duration.ZERO,
    acknowledge: Boolean = true,
): Flow<ReThisStreamMessage> = flow {
    val options = buildList {
        if (block.isPositive()) add(XReadGroupOption.Block(block))
        add(XReadGroupOption.Count(batchSize))
    }.toTypedArray()
    val streams = XReadGroupKeyIds(listOf(key), listOf(">"))

    while (coroutineContext.isActive) {
        val response = try {
            xReadGroup(group, consumer, streams, *options)
        } catch (_: Exception) {
            null
        }

        if (response.isNullOrEmpty()) {
            if (!block.isPositive()) delay(100)
            yield()
            continue
        }

        for (streamData in response) {
            val streamName = streamData.stream
            for (msg in streamData.messages) {
                if (acknowledge) {
                    xAck(streamName, group, msg.id)
                }
                emit(ReThisStreamMessage(msg.id, streamName, msg.data))
            }
        }

        if (!block.isPositive()) delay(100)
        yield()
    }
}
