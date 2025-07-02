package eu.vendeli.rethis.providers

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.utils.coRunBlocking
import kotlinx.io.Buffer

internal abstract class ConnectionProvider {
    abstract suspend fun execute(request: CommandRequest): Buffer

    init {
        coRunBlocking { initialize() }
    }

    open suspend fun initialize() {}
    abstract fun close()
    abstract suspend fun closeGracefully()

    abstract suspend fun borrowConnection(slot: Int? = null): RConnection
    abstract suspend fun releaseConnection(conn: RConnection)
}
