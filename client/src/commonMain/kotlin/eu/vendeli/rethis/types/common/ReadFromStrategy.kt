package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.CommandRequest

sealed interface ReadFromStrategy {
    fun pick(request: CommandRequest, snapshot: Snapshot): ConnectionProvider
}
