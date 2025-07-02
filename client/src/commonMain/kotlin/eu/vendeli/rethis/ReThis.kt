package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisDSL
import eu.vendeli.rethis.configuration.AuthConfiguration
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.core.ActiveSubscriptions
import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.providers.StandaloneProvider
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.types.common.UrlAddress
import eu.vendeli.rethis.utils.CLIENT_NAME
import eu.vendeli.rethis.utils.DEFAULT_HOST
import eu.vendeli.rethis.utils.DEFAULT_PORT
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@ReThisDSL
class ReThis internal constructor(
    internal val cfg: ReThisConfiguration,
    providerBlock: ReThis.() -> ConnectionProvider,
) {
    internal val logger = KtorSimpleLogger("eu.vendeli.rethis.ReThis")
    internal val rootJob = SupervisorJob()
    internal val connectionFactory = ConnectionFactory(cfg, rootJob)
    internal val provider = providerBlock(this)

    private val scope = CoroutineScope(rootJob + cfg.dispatcher + CoroutineName(CLIENT_NAME))

    val subscriptions = ActiveSubscriptions()

    suspend fun shutdownGracefully() {
        logger.info("Shutting down gracefully")

        subscriptions.unsubscribeAll()
        provider.closeGracefully()
        scope.cancel()
    }

    fun shutdown() {
        logger.info("Shutting down")

        subscriptions.unsubscribeAll()
        provider.close()
        scope.cancel()
    }

    // todo pipeline
    // todo transaction

    companion object {
        fun standalone(
            address: Address,
            protocol: RespVer = RespVer.V2,
            configurator: StandaloneConfiguration.() -> Unit = {},
        ): ReThis {
            val cfg = StandaloneConfiguration(protocol)
            cfg.configurator()

            return ReThis(cfg) { StandaloneProvider(address.socket, this) }
        }
    }
}

fun ReThis(
    host: String = DEFAULT_HOST,
    port: Int = DEFAULT_PORT,
    protocol: RespVer = RespVer.V2,
    configurator: StandaloneConfiguration.() -> Unit = {},
) = ReThis.standalone(Address(host, port), protocol, configurator)

fun ReThis(
    address: String,
    protocol: RespVer = RespVer.V2,
    configurator: StandaloneConfiguration.() -> Unit = {},
): ReThis {
    val addr = UrlAddress(address)
    val cfg = StandaloneConfiguration(protocol)
    cfg.db = addr.db
    addr.credentials.takeIf { it.isNotEmpty() }?.also { credentials ->
        cfg.auth = AuthConfiguration(
            credentials.first().toCharArray(), credentials.takeIf { it.size > 1 }?.last(),
        )
    }

    cfg.configurator()

    return ReThis(cfg) { StandaloneProvider(addr.address.socket, this) }
}
