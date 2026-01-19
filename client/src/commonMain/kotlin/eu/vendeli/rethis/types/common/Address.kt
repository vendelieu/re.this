package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.utils.DEFAULT_HOST
import eu.vendeli.rethis.utils.DEFAULT_PORT
import io.ktor.network.sockets.*
import kotlin.jvm.JvmInline

@JvmInline
value class Address(
    val socket: SocketAddress,
)

inline fun Address(host: String, port: Int) = Address(InetSocketAddress(host, port))
inline fun Address(socketPath: String) = Address(UnixSocketAddress(socketPath))
inline fun SocketAddress.asAddress() = Address(this)

internal class UrlAddress(
    url: String,
) {
    private val redisRegex = Regex(
        "^redis://(?:([^:]*)(?::([^@]*))?@)?([^:/?#]+)(?::(\\d+))?(/(\\d+))?(\\?([^#]*))?\$",
    )
    private val urlMatch = redisRegex.find(url)

    val address = Address(
        InetSocketAddress(
            hostname = urlMatch?.groups?.get(3)?.value ?: DEFAULT_HOST,
            port = urlMatch
                ?.groups
                ?.get(4)
                ?.value
                ?.toIntOrNull() ?: DEFAULT_PORT,
        ),
    )
    internal val credentials = listOfNotNull(urlMatch?.groups?.get(1)?.value, urlMatch?.groups?.get(2)?.value)
    internal val db = urlMatch
        ?.groups
        ?.get(6)
        ?.value
        ?.toIntOrNull() ?: 0
    internal val parameters = urlMatch
        ?.groups
        ?.get(8)
        ?.value
        ?.split('&')
        ?.associate { entry ->
            entry.split('&').let { it.first() to it.last() }
        }.orEmpty()
}
