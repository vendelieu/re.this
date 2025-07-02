package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.annotations.ConfigurationDSL
import eu.vendeli.rethis.types.common.RespVer
import io.ktor.network.tls.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@ConfigurationDSL
sealed class ReThisConfiguration(internal val protocol: RespVer) {
    internal var auth: AuthConfiguration? = null
    internal var tls: TLSConfig? = null
    internal var socket: SocketConfiguration = SocketConfiguration()
    internal var pool: PoolConfiguration = PoolConfiguration()
    internal var retry: RetryConfiguration = RetryConfiguration()
    internal open val withSlots = false

    var db: Int? = null
    var charset: Charset = Charsets.UTF_8
    var dispatcher: CoroutineDispatcher = Dispatchers.Default
    var gracefulShutdownPeriod: Duration = 40.seconds
    var maxConnections: Int = 5000

    fun auth(password: CharArray, username: String? = null) {
        auth = AuthConfiguration(password, username)
    }

    fun tls(block: () -> TLSConfig) {
        tls = block.invoke()
    }

    fun pool(block: PoolConfiguration.() -> Unit) {
        pool.block()
    }

    fun retry(block: RetryConfiguration.() -> Unit) {
        retry.block()
    }
}
