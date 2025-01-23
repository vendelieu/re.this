package eu.vendeli.rethis.types.core

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.sync.Mutex

data class RConnection(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
    val status: Mutex = Mutex(),
)

fun Socket.rConnection(): RConnection = RConnection(this, openReadChannel(), openWriteChannel())
