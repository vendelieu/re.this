package eu.vendeli.rethis.utils

import eu.vendeli.rethis.api.spec.common.types.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readLine
import kotlinx.io.readString

fun Buffer.tryInferCause(code: RespCode): ReThisException? = when (code) {
    RespCode.SIMPLE_ERROR -> readLine()?.tryInferSimpleError()
    RespCode.BULK_ERROR -> {
        val error = Buffer().also { readAvailable(it) }.readString()
        val cause = error.tryInferSimpleError()
        cause ?: ReThisException("Unknown error: $error")
    }

    else -> null
}

private fun String.tryInferSimpleError(): ReThisException? = when {
    startsWith("CROSSSLOT") -> CrossSlotOperationException("CROSSSLOT operation")
    startsWith("TRYAGAIN") -> RedirectUnstableException("TRYAGAIN redirection required", this)
    startsWith("ASK") -> {
        // Parse "ASK <slot> <host>:<port>"
        val parts = split(" ")
        val slot = parts[1].toInt()
        val newHostPort = parts[2].split(":")
        val newHost = newHostPort[0]
        val newPort = newHostPort[1].toInt()

        RedirectAskException(message = "ASK redirection required", slot = slot, host = newHost, port = newPort)
    }

    startsWith("MOVED") -> {
        // Parse "MOVED <slot> <host>:<port>"
        val parts = split(" ")
        val slot = parts[1].toInt()
        val newHostPort = parts[2].split(":")
        val newHost = newHostPort[0]
        val newPort = newHostPort[1].toInt()

        RedirectMovedException(message = "MOVED redirection required", slot = slot, host = newHost, port = newPort)
    }

    startsWith("CLUSTERDOWN") && this.contains("-UNBOUND") -> DownUnboundSlotException(
        "CLUSTERDOWN, unbound slot",
        this,
    )

    startsWith("CLUSTERDOWN") && this.contains("-READONLY") -> DownReadOnlyStateException(
        "CLUSTERDOWN, allow reads",
        this,
    )

    else -> ReThisException(this)
}
