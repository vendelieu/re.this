package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.SentinelConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RMap
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.types.common.SentinelSnapshot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SentinelTopologyManagerTest : TestCtx() {
    private val logger = SentinelConfiguration(RespVer.V2).loggerFactory.get("test")

    private class StubProvider(
        override val node: Address,
    ) : ConnectionProvider() {
        override suspend fun execute(request: CommandRequest): Buffer = Buffer()
        override fun close() {}
        override suspend fun borrowConnection(): RConnection = throw NotImplementedError()
        override suspend fun releaseConnection(conn: RConnection) {}
        override fun disposeConnection(conn: RConnection) {}
        override fun hasSpareConnection(): Boolean = false
    }

    private val master = StubProvider(Address("10.0.0.1", 6379))
    private val downReplica = StubProvider(Address("10.0.0.2", 6379))
    private val healthyReplica = StubProvider(Address("10.0.0.3", 6379))
    private val providers = arrayOf<ConnectionProvider>(master, downReplica, healthyReplica)

    private val probeLatencies = mapOf(
        master.node to 50.milliseconds,
        healthyReplica.node to 10.milliseconds,
    )

    private fun readRequest() = CommandRequest(Buffer(), RedisOperation.READ, command = "GET")

    private suspend fun probe(provider: ConnectionProvider): Duration =
        probeLatencies[provider.node] ?: error("replica unreachable")

    @Test
    fun `failed latency probe is recorded as worst case and does not abort measurement`() = runBlocking {
        val latencies = measureLatencies(providers, logger, ::probe)

        latencies.keys shouldBe setOf(0, 1, 2)
        latencies[0] shouldBe 50.milliseconds
        latencies[1] shouldBe Duration.INFINITE
        latencies[2] shouldBe 10.milliseconds
    }

    @Test
    fun `lowest latency never picks a replica whose probe failed`() = runBlocking {
        val snapshot = SentinelSnapshot(0, providers)
        snapshot.latencies.putAll(measureLatencies(providers, logger, ::probe))

        val request = readRequest()
        ReadFrom.LowestLatency.pick(request, snapshot) shouldBe healthyReplica
    }

    @Test
    fun `lowest latency falls back to the master when every probe failed`() = runBlocking {
        val snapshot = SentinelSnapshot(0, providers)
        snapshot.latencies.putAll(measureLatencies(providers, logger) { error("unreachable") })

        val request = readRequest()
        ReadFrom.LowestLatency.pick(request, snapshot) shouldBe master
    }

    private suspend fun awaitUntil(condition: suspend () -> Boolean) = withTimeout(10.seconds) {
        while (!condition()) delay(25.milliseconds)
    }

    private suspend fun withSentinelClient(
        block: suspend (sentinel: FakeRespServer, dataNode: FakeRespServer, client: ReThis) -> Unit,
    ) {
        FakeRespServer().use { dataNode ->
            FakeRespServer(masterReply = FakeRespServer.masterAddrReply(dataNode.address)).use { sentinel ->
                val client = ReThis.sentinel("mymaster", listOf(sentinel.address), RespVer.V2) {
                    periodicRefresh = true
                    periodicRefreshInterval = 1.hours
                }
                try {
                    awaitUntil { sentinel.commandCount("SENTINEL", "GET-MASTER-ADDR-BY-NAME") == 1 }
                    block(sentinel, dataNode, client)
                } finally {
                    client.close()
                }
            }
        }
    }

    @Test
    fun `switch-master subscription targets the sentinels while periodic refresh is enabled`() = runBlocking {
        withSentinelClient { sentinel, dataNode, _ ->
            awaitUntil { "+switch-master" in sentinel.subscribedChannels() }

            sentinel.subscribedChannels() shouldBe setOf("+switch-master")
            dataNode.commandCount("SUBSCRIBE") shouldBe 0
        }
    }

    @Test
    fun `switch-master message for the monitored master triggers exactly one refresh`() = runBlocking {
        withSentinelClient { sentinel, _, _ ->
            awaitUntil { "+switch-master" in sentinel.subscribedChannels() }

            sentinel.publish("+switch-master", "mymaster 10.0.0.1 6379 10.0.0.2 6379")
            awaitUntil { sentinel.commandCount("SENTINEL", "GET-MASTER-ADDR-BY-NAME") == 2 }
            delay(300.milliseconds)

            sentinel.commandCount("SENTINEL", "GET-MASTER-ADDR-BY-NAME") shouldBe 2
        }
    }

    @Test
    fun `switch-master message for another master does not trigger a refresh`() = runBlocking {
        withSentinelClient { sentinel, _, _ ->
            awaitUntil { "+switch-master" in sentinel.subscribedChannels() }

            sentinel.publish("+switch-master", "other 10.0.0.1 6379 10.0.0.2 6379")
            delay(300.milliseconds)

            sentinel.commandCount("SENTINEL", "GET-MASTER-ADDR-BY-NAME") shouldBe 1
        }
    }

    @Test
    fun `replicas reported down by sentinel are left out of the read pool`() = runBlocking {
        FakeRespServer().use { master ->
            FakeRespServer().use { healthy ->
                val socketOf = { server: FakeRespServer -> server.address.socket as InetSocketAddress }
                val replicas = FakeRespServer.array(
                    FakeRespServer.replicaEntry("127.0.0.1", 1, "s_down,slave,disconnected"),
                    FakeRespServer.replicaEntry("127.0.0.1", socketOf(healthy).port, "slave"),
                    FakeRespServer.replicaEntry("127.0.0.1", 2, "o_down,slave"),
                )
                FakeRespServer(FakeRespServer.masterAddrReply(master.address), replicas).use { sentinel ->
                    val client = ReThis.sentinel("mymaster", listOf(sentinel.address), RespVer.V2) {
                        readFromStrategy = ReadFrom.ReplicaPreferred
                    }
                    try {
                        awaitUntil { runCatching { client.topology.route(readRequest()) }.isSuccess }

                        val picked = List(20) { client.topology.route(readRequest()).node }.toSet()
                        picked shouldBe setOf(healthy.address)
                    } finally {
                        client.close()
                    }
                }
            }
        }
    }

    private fun respMap(ip: String?, port: String?, flags: String?) = RMap(
        listOfNotNull(
            "name" to "$ip:$port",
            ip?.let { "ip" to it },
            port?.let { "port" to it },
            flags?.let { "flags" to it },
        ).associate { (k, v) -> BulkString(k) to BulkString(v) },
    )

    private fun respFlat(vararg fields: String): RType = RArray(fields.map { BulkString(it) })

    @Test
    fun `resp3 replica maps keep only healthy replicas`() {
        val entries = listOf(
            respMap("10.0.0.1", "6379", "slave"),
            respMap("10.0.0.2", "6379", "s_down,slave"),
            respMap("10.0.0.3", "6379", "o_down,slave"),
            respMap("10.0.0.4", "6379", "slave,disconnected"),
        )

        parseReplicaAddresses(entries, logger) shouldBe listOf(Address("10.0.0.1", 6379))
    }

    @Test
    fun `resp2 flat replica arrays are read by field name regardless of order`() {
        val entries = listOf(
            respFlat("flags", "slave", "port", "6380", "runid", "abc", "ip", "10.0.0.5"),
        )

        parseReplicaAddresses(entries, logger) shouldBe listOf(Address("10.0.0.5", 6380))
    }

    @Test
    fun `short or unexpected replica entries are skipped without throwing`() {
        val entries = listOf(
            respFlat("name", "10.0.0.6:6379", "ip"),
            respFlat(),
            respMap("10.0.0.7", null, "slave"),
            respMap("10.0.0.8", "not-a-port", "slave"),
            respMap(null, "6379", "slave"),
            respMap("10.0.0.9", "6379", null),
            PlainString("10.0.0.10:6379 slave"),
            respMap("10.0.0.11", "6379", "slave"),
        )

        parseReplicaAddresses(entries, logger) shouldBe listOf(Address("10.0.0.11", 6379))
    }

    @Test
    fun `integer typed replica port is kept and entries without ip or flags are still skipped`() {
        val entries = listOf(
            RMap(
                mapOf(
                    BulkString("ip") to BulkString("10.0.0.12"),
                    BulkString("port") to Int64(6380),
                    BulkString("flags") to BulkString("slave"),
                ),
            ),
            RMap(mapOf(BulkString("port") to Int64(6381), BulkString("flags") to BulkString("slave"))),
            RMap(mapOf(BulkString("ip") to BulkString("10.0.0.13"), BulkString("port") to Int64(6382))),
        )

        parseReplicaAddresses(entries, logger) shouldBe listOf(Address("10.0.0.12", 6380))
    }

    @Test
    fun `switch-master payload yields the announced master address`() {
        "mymaster 10.0.0.1 6379 10.0.0.2 6380".switchMasterTarget() shouldBe Address("10.0.0.2", 6380)
        "mymaster 10.0.0.1 6379".switchMasterTarget() shouldBe null
        "mymaster 10.0.0.1 6379 10.0.0.2 x".switchMasterTarget() shouldBe null
    }

    @Test
    fun `a latency probe that never finishes is recorded as infinite without waiting`() = runTest {
        val latencies = measureLatencies(arrayOf<ConnectionProvider>(master, healthyReplica), logger) {
            if (it === master) delay(Duration.INFINITE)
            10.milliseconds
        }

        latencies[0] shouldBe Duration.INFINITE
        latencies[1] shouldBe 10.milliseconds
        currentTime shouldBe LATENCY_PROBE_TIMEOUT.inWholeMilliseconds
    }

    private class CountingProvider(
        node: Address,
        val closed: AtomicInteger = AtomicInteger(),
    ) : ConnectionProvider() {
        override val node: Address = node
        override suspend fun execute(request: CommandRequest): Buffer = Buffer()
        override fun close() {
            closed.incrementAndGet()
        }

        override suspend fun borrowConnection(): RConnection = throw NotImplementedError()
        override suspend fun releaseConnection(conn: RConnection) {}
        override fun disposeConnection(conn: RConnection) {}
        override fun hasSpareConnection(): Boolean = false
    }

    @Test
    fun `providers stored concurrently are all kept and closed on shutdown`() = runBlocking {
        val registry = SentinelProviderRegistry()
        val created = List(200) { CountingProvider(Address("10.0.1.1", it + 1)) }

        created.map { provider -> async(kotlinx.coroutines.Dispatchers.Default) { registry.add(provider) } }.awaitAll()

        registry.providers shouldContainExactlyInAnyOrder created
        registry.closeAll()
        created.all { it.closed.get() == 1 } shouldBe true
    }

    @Test
    fun `providers added while the registry shuts down are each closed exactly once`() = runBlocking {
        repeat(20) {
            val registry = SentinelProviderRegistry()
            val created = List(50) { CountingProvider(Address("10.0.2.1", it + 1)) }

            val adds = created.map { provider ->
                async(
                    kotlinx.coroutines.Dispatchers.Default,
                ) { registry.add(provider) }
            }
            val closing = async(kotlinx.coroutines.Dispatchers.Default) { registry.closeAll() }
            adds.awaitAll()
            closing.await()

            created.all { it.closed.get() == 1 } shouldBe true
            registry.providers shouldBe emptyList()
        }
    }

    @Test
    fun `a provider added after shutdown is closed immediately and not stored`() {
        val registry = SentinelProviderRegistry()
        registry.closeAll()
        val late = CountingProvider(Address("10.0.1.2", 1))

        registry.add(late) shouldBe false

        late.closed.get() shouldBe 1
        registry.providers shouldBe emptyList()
    }

    @Test
    fun `failing to fetch the sentinel topology reports the last cause`() = runBlocking {
        FakeRespServer(masterReply = "-ERR No such master with that name\r\n").use { sentinel ->
            val client = ReThis.sentinel("mymaster", listOf(sentinel.address), RespVer.V2) {
                pool {
                    minIdleConnections =
                        0
                }
            }
            try {
                val manager = client.topology as SentinelTopologyManager

                val error = shouldThrow<IllegalStateException> { manager.fetchSentinelTopology() }

                error.cause.shouldNotBeNull()
            } finally {
                client.close()
            }
        }
    }
}
