package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.stream.xAck
import eu.vendeli.rethis.command.stream.xAutoClaim
import eu.vendeli.rethis.command.stream.xReadGroup
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RMap
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.stream.XReadGroupMessage
import eu.vendeli.rethis.shared.types.stream.XReadGroupResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import kotlinx.io.readString
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

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
 * @param acknowledge whether to ACK each message after `emit` returned normally (at-least-once).
 * A message whose emit did not return normally (the collector threw, was cancelled, or a downstream
 * operator aborted the flow, e.g. the last element of `take(n)`) is not acknowledged and stays pending.
 *
 * On every start the flow first re-reads this consumer's own pending entries (delivered earlier to the same
 * [consumer] name but never acknowledged, e.g. after a failed or cancelled emit or a restart), emits and then
 * acknowledges each of them, and only then switches to new messages (`>`). Delivery is therefore at-least-once:
 * an entry processed by a collector that crashed before the acknowledgment is seen again. With
 * `acknowledge = false` the pending entries are delivered (once per start) but remain pending, so they are
 * delivered again on the next start. A pending entry that was deleted from the stream (`XDEL`, trimming) is skipped and
 * acknowledged (when [acknowledge] is set) instead of being emitted.
 *
 * Entries pending for other consumers are not taken over unless [claimIdleAfter] is set. When it is, the flow
 * additionally takes over, with `XAUTOCLAIM`, every entry that has been pending for any consumer (including this
 * one) for longer than [claimIdleAfter]: once at start, after the own-pending drain, and then about every
 * 5 seconds between reads (so at most once per [block] period). The claim pages with the returned cursor until it
 * is `0-0`. Claimed entries are emitted and acknowledged like new ones. This is at-least-once as well: the
 * original consumer may still process an entry that was taken over, so the processing must be idempotent, and with
 * `acknowledge = false` a claimed entry stays pending and is claimed again once it has been idle for
 * [claimIdleAfter]. Requires Redis 6.2 or newer.
 *
 * @param claimIdleAfter minimum idle time of an entry pending for another consumer before this flow takes it
 * over; `null` (default) disables the takeover
 */
@Suppress("FunctionName")
fun ReThis.StreamFlow(
    key: String,
    group: String,
    consumer: String,
    batchSize: Long = 10,
    block: Duration = Duration.ZERO,
    acknowledge: Boolean = true,
    claimIdleAfter: Duration? = null,
): Flow<ReThisStreamMessage> = flow {
    requireValidClaimIdle(claimIdleAfter)
    val options = buildList {
        if (block.isPositive()) add(XReadGroupOption.Block(block))
        add(XReadGroupOption.Count(batchSize))
    }.toTypedArray()
    val historyOptions = arrayOf<XReadGroupOption>(XReadGroupOption.Count(batchSize))

    suspend fun FlowCollector<ReThisStreamMessage>.deliver(response: List<XReadGroupResponse>) {
        for (streamData in response) {
            val streamName = streamData.stream
            for (msg in streamData.messages) {
                if (msg.data.isEmpty()) {
                    // deleted while pending: the entry has no fields (an XADD always stores at least one)
                    if (acknowledge) xAck(streamName, group, msg.id)
                    continue
                }
                emit(ReThisStreamMessage(msg.id, streamName, msg.data))
                if (acknowledge) {
                    xAck(streamName, group, msg.id)
                }
            }
        }
    }

    // Phase 1: this consumer's own pending history (id other than `>` never blocks and ends with an empty reply).
    // The cursor advances past the last delivered id so entries that stay pending (acknowledge = false) are not re-read.
    var cursor = PENDING_HISTORY_START
    while (coroutineContext.isActive) {
        val history = xReadGroup(group, consumer, XReadGroupKeyIds(listOf(key), listOf(cursor)), *historyOptions)
        val lastId = history
            ?.lastOrNull { it.messages.isNotEmpty() }
            ?.messages
            ?.last()
            ?.id ?: break
        deliver(history)
        cursor = lastId
        yield()
    }

    // Takeover of entries idle for other consumers (opt-in), paged by the returned cursor until it is 0-0.
    suspend fun FlowCollector<ReThisStreamMessage>.claimIdle(minIdle: Duration) {
        var claimCursor = CLAIM_START
        while (coroutineContext.isActive) {
            val reply = xAutoClaim(
                key,
                group,
                consumer,
                minIdle.inWholeMilliseconds.toString(),
                claimCursor,
                batchSize,
                null,
            )
            val page = parseAutoClaim(reply)
            if (page.messages.isNotEmpty()) deliver(listOf(XReadGroupResponse(key, page.messages)))
            if (page.nextCursor == CLAIM_START) break
            claimCursor = page.nextCursor
            yield()
        }
    }

    val claimClock = TimeSource.Monotonic
    var lastClaim = claimClock.markNow()
    if (claimIdleAfter != null) claimIdle(claimIdleAfter)

    // Phase 2: new messages.
    val streams = XReadGroupKeyIds(listOf(key), listOf(">"))
    while (coroutineContext.isActive) {
        if (claimIdleAfter != null && lastClaim.elapsedNow() >= CLAIM_INTERVAL) {
            claimIdle(claimIdleAfter)
            lastClaim = claimClock.markNow()
        }
        val response = xReadGroup(group, consumer, streams, *options)

        if (response.isNullOrEmpty()) {
            if (!block.isPositive()) delay(100)
            yield()
            continue
        }

        deliver(response)
        yield()
    }
}

private const val PENDING_HISTORY_START = "0"
private const val CLAIM_START = "0-0"

// the takeover is only checked between reads, so the effective cadence is max(CLAIM_INTERVAL, block)
private val CLAIM_INTERVAL = 5.seconds

internal class AutoClaimPage(
    val nextCursor: String,
    val messages: List<XReadGroupMessage>,
)

// XAUTOCLAIM reply: [next cursor, [[id, [field, value, ...] | nil], ...], [deleted ids] (Redis 7+)]
internal fun parseAutoClaim(reply: List<RType>): AutoClaimPage {
    val cursor = reply.firstOrNull().text() ?: error("Invalid XAUTOCLAIM response: missing cursor")
    val entries = (reply.getOrNull(1) as? RArray)?.value.orEmpty()
    val messages = entries.map { entry ->
        val parts = (entry as? RArray)?.value ?: error("Invalid XAUTOCLAIM response: entry is not an array")
        val id = parts.firstOrNull().text() ?: error("Invalid XAUTOCLAIM response: entry without id")
        val data = when (val raw = parts.getOrNull(1)) {
            is RArray -> raw.value.chunked(2).associate { pair ->
                val field = pair[0].text().orEmpty()
                val value = pair.getOrNull(1) ?: RType.Null
                field to value
            }

            is RMap -> raw.value.entries.associate { it.key.text().orEmpty() to (it.value ?: RType.Null) }

            else -> emptyMap()
        }
        XReadGroupMessage(id, data)
    }
    return AutoClaimPage(cursor, messages)
}

private fun RType?.text(): String? = when (this) {
    null -> null
    is BulkString -> value.readString()
    else -> value.toString()
}

private fun requireValidClaimIdle(claimIdleAfter: Duration?) {
    require(claimIdleAfter == null || !claimIdleAfter.isNegative()) {
        "claimIdleAfter must not be negative, given $claimIdleAfter"
    }
}
