package eu.vendeli.rethis.commands

import eu.vendeli.rethis.shared.types.CommandRequest
import kotlinx.io.readByteArray

/** Splits the encoded request into its RESP bulk-string arguments, in wire order (command name first). */
internal fun CommandRequest.respArguments(): List<String> = data
    .readByteArray()
    .decodeToString()
    .split("\r\n")
    .filter { it.isNotEmpty() && !it.startsWith("*") && !it.startsWith("$") }
