package eu.vendeli.rethis.configuration

import eu.vendeli.rethis.annotations.ConfigurationDSL
import eu.vendeli.rethis.core.DefaultLoggerFactory
import eu.vendeli.rethis.types.common.LoggerFactory
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.common.ReadFromStrategy
import eu.vendeli.rethis.types.common.RespVer
import io.ktor.network.tls.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents the configuration for a Redis client, allowing customization of connection parameters,
 * authentication, pooling, retry mechanisms, socket options, and more. This class serves as the
 * base for defining configurations using the DSL approach.
 *
 * @property usePooling Determines whether connection pooling is enabled. Defaults to true.
 * @property readFromStrategy Configures the preferred read strategy from the Redis instances (e.g., Master, Replica).
 * Defaults to [ReadFrom.Master].
 * @property db Specifies the Redis database index to use. If null, the default database is used.
 * @property charset Defines the character encoding for the connection. Defaults to UTF-8.
 * @property dispatcher The coroutine dispatcher used for performing operations. Defaults to [Dispatchers.Default].
 * @property maxConnections Maximum number of allowed connections in the pool. Defaults to 5000.
 * @property connectionAcquireTimeout Maximum time to wait for acquiring a connection from the pool. Defaults to 10 seconds.
 * @property loggerFactory Factory for creating loggers. Defaults to [DefaultLoggerFactory].
 */
@ConfigurationDSL
sealed class ReThisConfiguration(internal val protocol: RespVer) {
    internal var auth: AuthConfiguration? = null
    internal var tls: TLSConfig? = null
    internal var socket: SocketConfiguration = SocketConfiguration()
    internal var pool: PoolConfiguration = PoolConfiguration()
    internal var retry: RetryConfiguration = RetryConfiguration()
    internal open val withSlots = false

    /**
     * Determines whether connection pooling should be used.
     *
     * When enabled, a pool of connections will be maintained and reused, which can improve performance
     * and resource efficiency in applications with multiple connection requests. When disabled, new
     * connections will be created for each request, which may lead to higher resource consumption but
     * could be useful in specific use cases where pooling is not desirable.
     *
     * True by default.
     */
    var usePooling = true
    /**
     * Defines the strategy for selecting a connection to read data from within a Redis cluster or sentinel setup.
     * The selection strategy determines which node (master or replica) is chosen for handling read operations.
     *
     * By default, the strategy is set to `ReadFrom.Master`, which always chooses the master node for read requests.
     * Different strategies allow prioritization of replicas, latency optimization, or random selection.
     *
     * Available options include:
     * - `ReadFrom.Master`: Always reads from the master node.
     * - `ReadFrom.MasterPreferred`: Prefers master but falls back to a replica if necessary.
     * - `ReadFrom.Replica`: Always reads from a replica node.
     * - `ReadFrom.ReplicaPreferred`: Prefers a replica but falls back to the master.
     * - `ReadFrom.Any`: Selects a random node, including both master and replicas.
     * - `ReadFrom.AnyReplica`: Selects a random replica node (excludes master if possible).
     * - `ReadFrom.LowestLatency`: Chooses the node with the lowest latency.
     */
    var readFromStrategy: ReadFromStrategy = ReadFrom.Master

    /**
     * Specifies the Redis database index to connect to.
     *
     * By default, Redis uses database index 0. This property allows you to set a different database index
     * for connections. If the value is null, the default database is used.
     */
    var db: Int? = null
    /**
     * The character set used for encoding and decoding data in communication with the Redis server.
     *
     * By default, it is set to `Charsets.UTF_8`. This character set defines how text-based data is
     * interpreted and encoded, ensuring proper handling of international text and symbols.
     *
     * It is used in various encoding and decoding operations during communication with the Redis server,
     * such as preparing commands or handling responses.
     */
    var charset: Charset = Charsets.UTF_8
    /**
     * Specifies the [CoroutineDispatcher] to be used for executing general asynchronous operations.
     *
     * By default, the dispatcher is set to [Dispatchers.Default], which provides a shared pool of
     * threads optimized for compute-intensive tasks. This property can be customized to
     * enable the use of a specific dispatcher for tailored concurrency handling, such as a single-threaded
     * dispatcher or a custom thread pool.
     */
    var dispatcher: CoroutineDispatcher = Dispatchers.Default
    /**
     * Determines the maximum number of connections allowed generally (in pool, requests over pool, pubsub, and transaction mode).
     *
     * This value sets an upper limit on the number of concurrent connections that can
     * be maintained. The default value is set to 5000.
     */
    var maxConnections: Int = 5000
    /**
     * Specifies the maximum duration to wait when attempting to acquire a connection
     * from the connection provider before timing out.
     * This timeout applies to scenarios where no idle connections are currently available
     * and a new connection is being created or awaited.
     */
    var connectionAcquireTimeout: Duration = 10.seconds
    /**
     * Factory used for creating loggers within the configuration.
     *
     * This property allows customization of the logging mechanism by providing
     * an alternative implementation of the `LoggerFactory` interface. By default,
     * it uses `DefaultLoggerFactory`.
     */
    var loggerFactory: LoggerFactory = DefaultLoggerFactory

    /**
     * Configures authentication for the Redis connection by setting the password and optional username.
     *
     * @param password the password to use for authentication
     * @param username the username to use for authentication, defaults to null
     */
    fun auth(password: CharArray, username: String? = null) {
        auth = AuthConfiguration(password, username)
    }

    /**
     * Configures TLS settings for a redis connection.
     *
     * @param block A lambda that provides the TLS configuration. The lambda should return an instance of [TLSConfig].
     */
    fun tls(block: () -> TLSConfig) {
        tls = block.invoke()
    }

    /**
     * Configures the connection pool settings.
     *
     * @param block a lambda that receives an instance of [PoolConfiguration] to configure the pool's parameters
     */
    fun pool(block: PoolConfiguration.() -> Unit) {
        pool.block()
    }

    /**
     * Configures the retry mechanism used for operations, such as specifying
     * the number of retry attempts, delay between retries, and other related parameters.
     *
     * @param block Lambda with receiver of type [RetryConfiguration] to define retry settings.
     */
    fun retry(block: RetryConfiguration.() -> Unit) {
        retry.block()
    }

    /**
     * Configures the socket settings for the Redis connection.
     *
     * @param block a lambda with receiver that configures the socket options using the
     *              [SocketConfiguration] class
     */
    fun socket(block: SocketConfiguration.() -> Unit) {
        socket.block()
    }
}
