package eu.vendeli.rethis.types.core

import io.ktor.network.sockets.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual sealed class Address actual constructor() {
    internal actual abstract val socket: SocketAddress

    override fun toString() = stringify()
}

class UnixSocket(
    path: String,
) : Address() {
    override val socket = UnixSocketAddress(path)
}
