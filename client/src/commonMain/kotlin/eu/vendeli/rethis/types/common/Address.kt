package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.utils.Const.DEFAULT_HOST
import eu.vendeli.rethis.utils.Const.DEFAULT_PORT
import io.ktor.network.sockets.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect sealed class Address() {
    internal abstract val socket: SocketAddress
}

internal inline fun Address.stringify() = when (val thiz = socket) {
    is InetSocketAddress -> "InetSocket(${thiz.hostname}:${thiz.port})"
    is UnixSocketAddress -> "UnixSocket(${thiz.path})"
    else -> socket.toString()
}

class Host(
    host: String,
    port: Int,
) : Address() {
    override val socket = InetSocketAddress(host, port)
}

class Url(
    url: String,
) : Address() {
    private val redisRegex = Regex(
        "^redis://(?:([^:]*)(?::([^@]*))?@)?([^:/?#]+)(?::(\\d+))?(/(\\d+))?(\\?([^#]*))?\$",
    )
    private val urlMatch = redisRegex.find(url)

    override val socket = InetSocketAddress(
        hostname = urlMatch?.groups?.get(3)?.value ?: DEFAULT_HOST,
        port = urlMatch
            ?.groups
            ?.get(4)
            ?.value
            ?.toIntOrNull() ?: DEFAULT_PORT,
    )
    internal val credentials = listOfNotNull(urlMatch?.groups?.get(1)?.value, urlMatch?.groups?.get(2)?.value)
    internal val db = urlMatch
        ?.groups
        ?.get(6)
        ?.value
        ?.toIntOrNull() ?: 0
    internal val parameters = urlMatch?.groups?.get(8)?.value?.split('&')?.associate { entry ->
        entry.split('&').let { it.first() to it.last() }
    } ?: emptyMap()
}
