package eu.vendeli.rethis.types.interfaces

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.types.common.Snapshot

interface ReadFromStrategy {
    fun pick(request: CommandRequest, snapshot: Snapshot): ConnectionProvider
}
