package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.configuration.AuthConfiguration
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
    // Messages below must never contain the URL or parts of it: it can carry credentials.
    private val urlMatch = REDIS_URL_REGEX.find(url) ?: throw IllegalArgumentException(invalidUrlMessage(url))
    private val userInfoUser = urlMatch.groups[1]?.value
    private val userInfoPassword = urlMatch.groups[2]?.value

    /** True for the `rediss://` scheme: the connection must be wrapped in TLS. */
    internal val useTls: Boolean = url.startsWith(TLS_SCHEME_PREFIX)

    val address = Address(
        InetSocketAddress(
            hostname = urlMatch.groups[3]!!.value.removeSurrounding("[", "]"),
            port = urlMatch.groups[4]?.value?.let { parseNumber(it, "port", MIN_PORT, MAX_PORT) } ?: DEFAULT_PORT,
        ),
    )

    /**
     * Username of the `user:password@` form. Null when the user part is empty (`:password@`) or absent.
     * Percent-encoded characters are decoded.
     */
    internal val username: String? = if (userInfoPassword == null) {
        null
    } else {
        userInfoUser?.takeIf { it.isNotEmpty() }?.percentDecode()
    }

    /**
     * Password of the `user:password@` or `:password@` form. A user-info without a colon (`secret@`)
     * is treated as a password-only form. Percent-encoded characters are decoded.
     */
    internal val password: String? = (userInfoPassword ?: userInfoUser?.takeIf { it.isNotEmpty() })
        ?.percentDecode()

    internal val db = urlMatch.groups[6]?.value?.let { parseNumber(it, "db", 0, Int.MAX_VALUE) } ?: 0
    internal val parameters = urlMatch.groups[8]
        ?.value
        ?.split('&')
        ?.filter { it.isNotEmpty() }
        ?.associate { entry ->
            entry.substringBefore('=').percentDecode() to entry.substringAfter('=', "").percentDecode()
        }.orEmpty()

    /** Authentication settings described by the URL, or null when it carries no credentials. */
    internal fun authConfiguration(): AuthConfiguration? = password?.let {
        AuthConfiguration(
            it.toCharArray(),
            username,
        )
    }

    // Credentials must never leak through logs or exception messages.
    override fun toString(): String = "UrlAddress(address=$address, db=$db)"

    private companion object {
        const val MIN_PORT = 1
        const val MAX_PORT = 65535
        const val EXPECTED_FORMAT = "expected redis[s]://[[user]:password@]host[:port][/db]"
        const val TLS_SCHEME_PREFIX = "rediss://"
        val SCHEME_REGEX = Regex("^[A-Za-z][A-Za-z0-9+.-]*$")

        fun invalidUrlMessage(url: String): String {
            val scheme = url.substringBefore("://", "")
            return if (scheme.isNotEmpty() && SCHEME_REGEX.matches(scheme) && !isRedisScheme(scheme)) {
                "Invalid Redis URL: scheme '$scheme' is not supported; $EXPECTED_FORMAT"
            } else {
                "Invalid Redis URL; $EXPECTED_FORMAT"
            }
        }

        fun isRedisScheme(scheme: String) = scheme.equals("redis", true) || scheme.equals("rediss", true)

        fun parseNumber(raw: String, field: String, min: Int, max: Int): Int =
            raw.toIntOrNull()?.takeIf { it in min..max }
                ?: throw IllegalArgumentException("Invalid Redis URL: $field is out of range or not a number")

        val REDIS_URL_REGEX = Regex(
            "^rediss?://(?:([^:@/?#]*)(?::([^@/?#]*))?@)?" +
                "(\\[[^\\]]+\\]|[^:/?#\\[\\]@]+)(?::(\\d+))?(/(\\d+))?(\\?([^#]*))?\$",
        )
    }
}

private const val HEX_RADIX = 16
private const val HEX_DIGITS = 2

/** Decodes `%xx` sequences (RFC 3986) as UTF-8; malformed sequences are kept literally. `+` is not special. */
private fun String.percentDecode(): String {
    if ('%' !in this) return this
    val src = encodeToByteArray()
    val out = ArrayList<Byte>(src.size)
    var i = 0
    while (i < src.size) {
        val decoded = if (src[i] == PERCENT) hexByteAt(src, i + 1) else null
        if (decoded != null) {
            out.add(decoded)
            i += 1 + HEX_DIGITS
        } else {
            out.add(src[i])
            i++
        }
    }
    return out.toByteArray().decodeToString()
}

private const val PERCENT = '%'.code.toByte()

private fun hexByteAt(src: ByteArray, from: Int): Byte? {
    if (from + HEX_DIGITS > src.size) return null
    val hi = src[from].toInt().toChar().digitToIntOrNull(HEX_RADIX) ?: return null
    val lo = src[from + 1].toInt().toChar().digitToIntOrNull(HEX_RADIX) ?: return null
    return (hi * HEX_RADIX + lo).toByte()
}
