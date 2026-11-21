package eu.vendeli.rethis.core

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.isRetryableFor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import eu.vendeli.rethis.command.string.set

class ConnectionPoolTest : TestCtx() {
    private fun config(block: StandaloneConfiguration.() -> Unit = {}) = StandaloneConfiguration(RespVer.V2).apply {
        pool {
            minIdleConnections = 0
            maxIdleConnections = 4
            // keep the background observer out of the way: the tests drive the pool directly
            checkInterval = 1.hours
        }
        block()
    }

    private fun withPool(
        cfg: StandaloneConfiguration = config(),
        block: suspend (ConnectionPool) -> Unit,
    ) = FakeRespServer().use { server ->
        val rootJob = Job()
        val pool = ConnectionPool(server.address.socket, cfg, ConnectionFactory(cfg, rootJob), rootJob)
        try {
            runBlocking { block(pool) }
        } finally {
            pool.close()
            rootJob.cancel()
        }
    }

    @Test
    fun `idle count tracks the channel across repeated borrow and release cycles`() = withPool { pool ->
        repeat(10) {
            val conn = pool.acquire()
            pool.release(conn)
        }

        pool.idleCount shouldBe 1
    }

    @Test
    fun `idle count drops to zero while the only idle connection is borrowed`() = withPool { pool ->
        pool.release(pool.acquire())

        val borrowed = pool.acquire()

        pool.idleCount shouldBe 0
        pool.release(borrowed)
        pool.idleCount shouldBe 1
    }

    @Test
    fun `burst rate for a sub-second interval is finite`() {
        val rate = ConnectionPool.burstRate(borrows = 10, interval = 500.milliseconds)

        rate.isFinite() shouldBe true
        rate shouldBeExactly 20.0
    }

    @Test
    fun `burst rate keeps the fractional part of the interval`() {
        ConnectionPool.burstRate(borrows = 3, interval = 1500.milliseconds) shouldBeExactly 2.0
    }

    @Test
    fun `expand size under a sub-second interval never exceeds the idle capacity`() {
        val rate = ConnectionPool.burstRate(borrows = 100_000, interval = 500.milliseconds)

        val expandBy = ConnectionPool.expandSize(rate, idleNow = 1, idleCapacity = 4)

        expandBy shouldBeLessThanOrEqual 3
        expandBy shouldBe 3
    }

    @Test
    fun `expand size is zero without pressure or room`() {
        ConnectionPool.expandSize(burstRate = 0.0, idleNow = 0, idleCapacity = 4) shouldBe 0
        ConnectionPool.expandSize(burstRate = 50.0, idleNow = 4, idleCapacity = 4) shouldBe 0
    }

    @Test
    fun `non-positive check interval is rejected`() {
        listOf(Duration.ZERO, (-1).milliseconds).forEach { interval ->
            val cfg = config { pool { checkInterval = interval } }

            val ex = shouldThrow<IllegalArgumentException> {
                FakeRespServer().use { server ->
                    val rootJob = Job()
                    ConnectionPool(server.address.socket, cfg, ConnectionFactory(cfg, rootJob), rootJob)
                }
            }
            ex.message shouldContain "checkInterval"
        }
    }

    @Test
    fun `empty pool reports no idle connections`() = withPool { pool ->
        pool.haveIdleConnections() shouldBe false
    }

    @Test
    fun `pool holding an idle connection reports idle connections`() = withPool { pool ->
        pool.release(pool.acquire())

        pool.haveIdleConnections() shouldBe true
    }

    @Test
    fun `pool filled to maxIdleConnections still reports idle connections`() = withPool { pool ->
        val borrowed = List(4) { pool.acquire() }
        borrowed.forEach { pool.release(it) }

        pool.idleCount shouldBe 4
        pool.haveIdleConnections() shouldBe true
    }

    @Test
    fun `pool reports no idle connections once the last one is borrowed`() = withPool { pool ->
        pool.release(pool.acquire())

        val borrowed = pool.acquire()

        pool.haveIdleConnections() shouldBe false
        pool.release(borrowed)
    }

    @Test
    fun `idle count never goes negative and matches the channel under concurrent borrow and release`() =
        withPool(config { pool { maxIdleConnections = 4 } }) { pool ->
            // idle capacity covers every worker, so connections are reused instead of churned
            var minObserved = Int.MAX_VALUE
            withContext(Dispatchers.Default) {
                val sampler = launch {
                    while (isActive) minObserved = minOf(minObserved, pool.idleCount)
                }
                List(32) {
                    async {
                        repeat(3_000) { pool.release(pool.acquire()) }
                    }
                }.awaitAll()
                sampler.cancel()
            }

            minObserved shouldBeGreaterThanOrEqual 0
            // draining by borrowing exactly idleCount connections must leave the counter at zero
            val idle = pool.idleCount
            val drained = List(idle) { pool.acquire() }
            pool.idleCount shouldBe 0
            drained.forEach { pool.release(it) }
            pool.idleCount shouldBe idle
        }

    @Test
    fun `exhausting the pool for the acquire timeout fails with a timeout exception instead of a cancellation`() =
        withPool(
            config {
                maxConnections = 1
                connectionAcquireTimeout = 100.milliseconds
            },
        ) { pool ->
            val held = pool.acquire()

            val failure = shouldThrow<CommandTimeoutException> { pool.acquire() }

            failure.cause.shouldBeInstanceOf<TimeoutCancellationException>()
            failure.isRetryableFor(RedisOperation.READ) shouldBe true
            failure.isRetryableFor(RedisOperation.WRITE) shouldBe false
            pool.release(held)
        }

    @Test
    fun `a caller timeout during acquire is still a cancellation`() =
        withPool(config { maxConnections = 1 }) { pool ->
            val held = pool.acquire()

            shouldThrow<TimeoutCancellationException> { withTimeout(100.milliseconds) { pool.acquire() } }

            pool.release(held)
        }

    @Test
    fun `failed prepopulation is absorbed without an uncaught exception and the pool stays usable`() {
        val uncaught = java.util.concurrent.CopyOnWriteArrayList<Throwable>()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaught += e }
        try {
            val port = FakeRespServer().use { (it.address.socket as io.ktor.network.sockets.InetSocketAddress).port }
            val cfg = config { pool { minIdleConnections = 3 } }
            val rootJob = Job()
            val pool = ConnectionPool(
                io.ktor.network.sockets
                    .InetSocketAddress("127.0.0.1", port),
                cfg,
                ConnectionFactory(cfg, rootJob),
                rootJob,
            )
            try {
                runBlocking { kotlinx.coroutines.delay(500) }
                uncaught.toList() shouldBe emptyList()
                pool.idleCount shouldBe 0
            } finally {
                pool.close()
                rootJob.cancel()
            }
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
    }

    @Test
    fun `first acquire succeeds when prepopulation takes the only connection permit`() {
        FakeRespServer().use { server ->
            val socket = server.address.socket as io.ktor.network.sockets.InetSocketAddress
            // the race needs the acquire to arrive while a prepopulated connection is still handshaking
            repeat(10) {
                val client = eu.vendeli.rethis.ReThis(socket.hostname, socket.port, RespVer.V3) {
                    maxConnections = 1
                    connectionAcquireTimeout = 3.seconds
                }
                try {
                    runBlocking { client.set("k", "v") shouldBe "OK" }
                } finally {
                    client.close()
                }
            }
        }
    }
}
