package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.providers.ConnectionProvider

sealed interface ReadFromStrategy {
    fun pick(request: CommandRequest, snapshot: Snapshot): ConnectionProvider
}
