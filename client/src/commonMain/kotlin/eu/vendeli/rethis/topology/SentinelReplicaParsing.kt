package eu.vendeli.rethis.topology

import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.shared.utils.unwrapRESPAgnosticMap
import eu.vendeli.rethis.types.common.Address
import io.ktor.util.logging.*

private val DOWN_FLAGS = setOf("s_down", "o_down", "disconnected")
private const val REPLICA_FLAG = "slave"

/**
 * Extracts the addresses of the healthy replicas from a `SENTINEL REPLICAS` reply.
 *
 * Each entry is a field/value map (a flat key/value array on RESP2, a map on RESP3) carrying `ip`, `port` and a
 * comma separated `flags` string. Replicas flagged `s_down`, `o_down` or `disconnected` are left out, and entries
 * that are not field maps or lack a usable `ip`/`port`/`flags` are logged and skipped.
 */
internal fun parseReplicaAddresses(entries: List<RType>, logger: Logger): List<Address> = entries.mapNotNull { entry ->
    val fields = entry.toFieldMap(logger)
    val ip = fields?.get("ip")
    val port = fields?.get("port")?.toIntOrNull()
    val flags = fields?.get("flags")?.split(',')?.toSet()

    when {
        ip == null || port == null || flags == null -> {
            logger.warn("Skipping malformed SENTINEL REPLICAS entry: $entry")
            null
        }

        REPLICA_FLAG !in flags || flags.any { it in DOWN_FLAGS } -> {
            null
        }

        else -> {
            Address(ip, port)
        }
    }
}

private fun RType.toFieldMap(logger: Logger): Map<String, String?>? = try {
    if (this is RArray && value.size % 2 != 0) {
        null
    } else {
        unwrapRESPAgnosticMap<String, RType>()?.mapValues { (_, value) -> value?.asText() }
    }
} catch (e: Exception) {
    logger.debug("Unable to read SENTINEL REPLICAS entry as a field map", e)
    null
}

/** Text of a scalar field; integers are accepted too, since a numeric `port` is not guaranteed to be a string. */
private fun RType.asText(): String? = if (this is Int64) value.toString() else unwrap<String>()
