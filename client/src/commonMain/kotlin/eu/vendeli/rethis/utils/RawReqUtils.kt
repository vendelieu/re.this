package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

/**
 * Low-level execute function
 *
 * @param request [RESP](https://redis.io/docs/latest/develop/reference/protocol-spec/) encoded payload.
 * @param operationKind hint for routing about the operation type.
 * @param isBlocking hint for routing about the blocking mode.
 */
@ReThisInternal
suspend fun ReThis.execute(
    request: Buffer,
    operationKind: RedisOperation = RedisOperation.READ,
    isBlocking: Boolean = false,
): Buffer = CommandRequest(request, operationKind, isBlocking).let {
    topology
        .route(it)
        .execute(it)
}

/**
 * Convert list to RESP encoded payload.
 */
@ReThisInternal
fun List<Any?>.toRESPBuffer(charset: Charset = Charsets.UTF_8): Buffer {
    val buffer = Buffer()
    buffer.writeString("*$size\r\n")

    forEach { element ->
        when (element) {
            is String -> buffer.writeStringArg(element, charset)
            is Long -> buffer.writeLongArg(element, charset)
            is Int -> buffer.writeIntArg(element, charset)
            is ByteArray -> buffer.writeByteArrayArg(element, charset)
            is Boolean -> buffer.writeBooleanArg(element, charset)
            is Double -> buffer.writeDoubleArg(element, charset)

            null -> buffer.writeString("$-1\r\n")
            else -> throw IllegalArgumentException("Unsupported type: ${element::class}")
        }
    }
    return buffer
}

@ReThisInternal
suspend fun ReThis.execute(requestBlock: MutableList<Any?>.() -> Unit): Buffer =
    execute(buildList(requestBlock).toRESPBuffer())
