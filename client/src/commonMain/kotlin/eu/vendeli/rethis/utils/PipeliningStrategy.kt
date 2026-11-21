package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.providers.withConn
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.CrossSlotOperationException
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedirectMovedException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.topology.StandaloneTopologyManager
import eu.vendeli.rethis.types.common.RConnection
import kotlinx.io.Buffer
import kotlinx.io.writeString

/**
 * Sends [pipelined] in per-slot groups on a cluster. Only `MOVED` replies are followed (once); `ASK`, other
 * redirects and transport failures are not retried for pipelined commands and surface in the responses or as errors.
 */
internal suspend fun ReThis.handlePipelinedRequests(
    pipelined: List<CommandRequest>,
    ctxConn: RConnection?,
): List<RType> {
    if (topology is StandaloneTopologyManager) {
        return withConn(topology.provider, ctxConn) { conn -> conn.doRTypeRequest(this, pipelined) }
    }

    return executeBySlot(pipelined) { _, requests -> executeGroup(requests, ctxConn) }
}

private suspend fun ReThis.executeGroup(requests: List<CommandRequest>, ctxConn: RConnection?): List<RType> {
    val responses = withConn(topology.route(requests.routingRequest()), ctxConn) { it.doRTypeRequest(this, requests) }
    if (ctxConn != null) return responses

    // Other redirects, retries and ASK are not followed for pipelined commands; the error replies are returned as is.
    // Commands of a MULTI..EXEC block are never replayed one by one: outside the block they would run untransacted.
    val inTransaction = requests.transactionRegions().flatten().toSet()
    val moved = responses.mapIndexedNotNull { i, r ->
        if (i in inTransaction) return@mapIndexedNotNull null
        (r as? RType.Error)?.movedOrNull()?.let { i to it }
    }
    if (moved.isEmpty()) return responses

    // one retry of the moved commands: the failure handler points the slot at its new owner
    try {
        topology.handleFailure(requests[moved.first().first], moved.first().second)
    } catch (_: RedirectMovedException) {
        // expected: the owner is updated and the call is retried by the caller
    }
    val retried = moved.map { requests[it.first] }
    val retriedResponses = withConn(topology.route(retried.routingRequest()), null) {
        it.doRTypeRequest(this, retried)
    }
    val result = responses.toMutableList()
    moved.forEachIndexed { n, (i, _) -> result[i] = retriedResponses[n] }
    return result
}

private fun RType.Error.movedOrNull(): RedirectMovedException? =
    Buffer()
        .apply {
            writeString(exception.message.orEmpty())
        }.tryInferCause(RespCode.SIMPLE_ERROR) as? RedirectMovedException

/** A group is sent to one node, so a keyed write in it decides the target: a read-first group must not land on a replica. */
private fun List<CommandRequest>.routingRequest(): CommandRequest {
    // keyless requests (MULTI, EXEC, PING) carry no slot and would route to the first node
    val keyed = filter { it.computedSlot != null }.ifEmpty { this }
    return keyed.firstOrNull { it.operation == RedisOperation.WRITE } ?: keyed.first()
}

private const val MULTI = "MULTI"
private val TRANSACTION_END = setOf("EXEC", "DISCARD")

/** Index ranges of the `MULTI` .. `EXEC`/`DISCARD` blocks of [this]; an unterminated block runs to the end. */
private fun List<CommandRequest>.transactionRegions(): List<IntRange> = buildList {
    var start = -1
    this@transactionRegions.forEachIndexed { i, request ->
        when {
            start < 0 && request.command.equals(MULTI, ignoreCase = true) -> {
                start = i
            }

            start >= 0 && request.command.uppercase() in TRANSACTION_END -> {
                add(start..i)
                start = -1
            }
        }
    }
    if (start >= 0) add(start..this@transactionRegions.lastIndex)
}

/**
 * The slot every request is grouped under: its own computed slot, except that all requests of a `MULTI` block take
 * the slot of the block so that it is sent as one unit. A block whose commands address different slots cannot run
 * on one node and is rejected with [CrossSlotOperationException] before anything is sent.
 *
 * Only the block itself is checked: a `WATCH` placed before `MULTI` whose key belongs to another slot is grouped
 * under its own slot, sent separately from the block, and is not rejected.
 */
private fun List<CommandRequest>.groupingSlots(): List<Int?> {
    val slots = map { it.computedSlot }.toMutableList()
    transactionRegions().forEach { region ->
        val blockSlots = region.mapNotNull { slots[it] }.distinct()
        if (blockSlots.size > 1) throw CrossSlotOperationException(
            "A MULTI/EXEC block in a pipeline must use keys of a single slot, found slots ${blockSlots.joinToString()}",
        )
        region.forEach { slots[it] = blockSlots.firstOrNull() }
    }
    return slots
}

/**
 * Groups [pipelined] by slot, runs each group through [execute] and returns the
 * responses in the order of the original requests. A `MULTI`..`EXEC` block stays in one group, in order.
 */
internal suspend inline fun executeBySlot(
    pipelined: List<CommandRequest>,
    execute: (slot: Int?, requests: List<CommandRequest>) -> List<RType>,
): List<RType> {
    val slots = pipelined.groupingSlots()
    val groups = pipelined.withIndex().groupBy { slots[it.index] }
    val responses = arrayOfNulls<RType>(pipelined.size)

    groups.forEach { (slot, indexed) ->
        val groupResponses = execute(slot, indexed.map { it.value })
        check(groupResponses.size == indexed.size) {
            "Expected ${indexed.size} responses for the slot group, got ${groupResponses.size}"
        }
        indexed.forEachIndexed { i, (originalIndex, _) -> responses[originalIndex] = groupResponses[i] }
    }

    return responses.map { requireNotNull(it) }
}

private suspend inline fun RConnection.doRTypeRequest(client: ReThis, payload: List<CommandRequest>): List<RType> {
    val payloadBuffer = payload.map { it.data }
    val request = doBatchRequest(
        payloadBuffer,
        commandTimeout = client.cfg.commandTimeoutFor(payload),
    )
    return buildList {
        repeat(payloadBuffer.size) {
            add(RTypeDecoder.decode(request, client.cfg.charset))
        }
    }
}
