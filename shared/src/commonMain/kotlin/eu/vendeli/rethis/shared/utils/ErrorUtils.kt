package eu.vendeli.rethis.shared.utils

import eu.vendeli.rethis.shared.types.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLine

fun Buffer.tryInferCause(code: RespCode, charset: Charset = Charsets.UTF_8): ReThisException? = when (code) {
    RespCode.SIMPLE_ERROR -> readLine()?.tryInferSimpleError()
    RespCode.BULK_ERROR -> {
        val size = readDecimalCrlf().toInt()
        readSizedText(size, charset).tryInferSimpleError()
    }

    else -> null
}

// CLUSTERDOWN texts match redis src/cluster.c (clusterRedirectClient), verified on 7.2 and unstable.
private fun String.tryInferSimpleError(): ReThisException? = when {
    hasErrorCode("CROSSSLOT") -> CrossSlotOperationException(this)
    hasErrorCode("TRYAGAIN") -> RedirectUnstableException("TRYAGAIN redirection required", this)
    hasErrorCode("ASK") -> parseRedirect { slot, host, port ->
        RedirectAskException(message = "ASK redirection required", slot = slot, host = host, port = port)
    }

    hasErrorCode("MOVED") -> parseRedirect { slot, host, port ->
        RedirectMovedException(message = "MOVED redirection required", slot = slot, host = host, port = port)
    }

    hasErrorCode("CLUSTERDOWN") && contains("Hash slot not served") -> DownUnboundSlotException(
        "CLUSTERDOWN, unbound slot",
        this,
    )

    hasErrorCode("CLUSTERDOWN") && contains("only accepts read commands") -> DownReadOnlyStateException(
        "CLUSTERDOWN, allow reads",
        this,
    )

    else -> ReThisException(this)
}

/** True when the first space-delimited token of the error text is exactly [code]. */
private fun String.hasErrorCode(code: String): Boolean =
    startsWith(code) && (length == code.length || this[code.length] == ' ')

/**
 * Parses "<KIND> <slot> <host>:<port>", splitting host and port on the last colon so IPv6 hosts survive.
 * Malformed lines degrade to a plain [ReThisException] instead of leaking parsing exceptions.
 */
private inline fun String.parseRedirect(
    build: (slot: Int, host: String, port: Int) -> ReThisException,
): ReThisException {
    val parts = split(" ")
    val slot = parts.getOrNull(1)?.toIntOrNull()
    val hostPort = parts.getOrNull(2)
    val port = hostPort?.takeIf { ':' in it }?.substringAfterLast(':')?.toIntOrNull()
    if (slot == null || hostPort == null || port == null) return ReThisException(this)

    return build(slot, hostPort.substringBeforeLast(':'), port)
}
