package eu.vendeli.rethis.topology

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.ClusterException
import eu.vendeli.rethis.shared.types.RedirectAskException
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ClusterSnapshot
import kotlinx.io.Buffer
import kotlinx.io.readString

/**
 * Runs [block] against the node named by the ASK error. A provider already present in [snapshot] is reused;
 * otherwise a temporary one is created with [create] and closed afterwards. The snapshot is never modified,
 * since an ASK redirect applies to a single request only.
 */
internal suspend fun <R> withAskTarget(
    snapshot: ClusterSnapshot,
    exception: RedirectAskException,
    create: (Address) -> ConnectionProvider,
    block: suspend (ConnectionProvider) -> R,
): R {
    val target = Address(exception.host, exception.port)
    val known = snapshot.providers.firstOrNull { it.node == target }
    if (known != null) return block(known)

    val temporary = create(target)
    try {
        return block(temporary)
    } finally {
        temporary.close()
    }
}

private const val ERROR_PREFIX = '-'.code.toByte()

/**
 * Sends ASKING followed by the command through [exchange] (one request, one reply) and returns
 * only the command's reply; the ASKING reply is consumed and an error reply to it is thrown.
 */
internal suspend fun requestAfterAsking(
    asking: Buffer,
    command: Buffer,
    exchange: suspend (Buffer) -> Buffer,
): Buffer {
    val askingReply = exchange(asking)
    if (!askingReply.exhausted() && askingReply.copy().readByte() == ERROR_PREFIX) {
        throw AskingRejectedException("ASKING rejected: ${askingReply.readString().trim()}")
    }
    return exchange(command)
}

/** The node answered ASKING with an error reply; the connection itself is still protocol-clean. */
internal class AskingRejectedException(
    message: String,
) : ClusterException(message)

/**
 * Like [requestAfterAsking], but a rejected ASKING is returned as a failed [Result] instead of thrown, so the caller
 * can leave the connection scope normally (releasing the clean connection) and raise the failure afterwards.
 */
internal suspend fun requestAfterAskingCatching(
    asking: Buffer,
    command: Buffer,
    exchange: suspend (Buffer) -> Buffer,
): Result<Buffer> = try {
    Result.success(requestAfterAsking(asking, command, exchange))
} catch (e: AskingRejectedException) {
    Result.failure(e)
}
