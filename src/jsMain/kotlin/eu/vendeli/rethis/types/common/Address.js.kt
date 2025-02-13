package eu.vendeli.rethis.types.common

import io.ktor.network.sockets.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual sealed class Address actual constructor() {
    internal actual abstract val socket: SocketAddress

    override fun toString() = stringify()
}
