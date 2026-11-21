package eu.vendeli.rethis.topology

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.ClusterException
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class OnDemandRefreshTest : TestCtx() {
    private val snapshot = AtomicReference<String?>(null)
    private val runs = AtomicInteger()

    @BeforeEach
    fun reset() {
        snapshot.set(null)
        runs.set(0)
    }

    @Test
    fun `concurrent callers share a single refresh`() = runBlocking(Dispatchers.Default) {
        val onDemand = OnDemandRefresh(Mutex())

        val results = (1..8)
            .map {
                async {
                    onDemand.ensure(snapshot::get) {
                        runs.incrementAndGet()
                        delay(50)
                        snapshot.set("ready")
                    }
                }
            }.awaitAll()

        runs.get() shouldBe 1
        results.all { it == "ready" } shouldBe true
    }

    @Test
    fun `callers fail fast with the cause during the cool-down after a failed refresh`() = runBlocking(
        Dispatchers.Default,
    ) {
        val onDemand = OnDemandRefresh(Mutex(), coolDown = 10.seconds)

        val outcomes = (1..5)
            .map {
                async {
                    runCatching {
                        onDemand.ensure(snapshot::get) {
                            runs.incrementAndGet()
                            delay(20)
                            error("seed down")
                        }
                    }.exceptionOrNull()
                }
            }.awaitAll()

        runs.get() shouldBe 1
        outcomes.all { it is ClusterException && it.cause?.message == "seed down" } shouldBe true
    }

    @Test
    fun `refreshes again once the cool-down has passed`() = runBlocking {
        val onDemand = OnDemandRefresh(Mutex(), coolDown = 20.milliseconds)
        runCatching {
            onDemand.ensure(snapshot::get) {
                runs.incrementAndGet()
                error("down")
            }
        }
        delay(50)

        onDemand.ensure(snapshot::get) {
            runs.incrementAndGet()
            snapshot.set("ready")
        } shouldBe "ready"
        runs.get() shouldBe 2
    }

    @Test
    fun `a refresh that times out is a failed refresh with a cool-down`() = runBlocking {
        val onDemand = OnDemandRefresh(Mutex(), coolDown = 10.seconds)

        val failure = runCatching {
            onDemand.ensure(snapshot::get) {
                runs.incrementAndGet()
                withTimeout(10.milliseconds) { delay(10.seconds) }
            }
        }.exceptionOrNull()

        failure.shouldBeInstanceOf<ClusterNotReadyException>()
        failure.message shouldBe "Cluster not ready"
        failure.cause.shouldBeInstanceOf<TimeoutCancellationException>()
    }

    @Test
    fun `a caller arriving during the cool-down waits it out once and then refreshes`() = runTest {
        val onDemand = OnDemandRefresh(Mutex(), coolDown = 10.seconds, timeSource = testTimeSource)
        runCatching { onDemand.ensure(snapshot::get) { runs.incrementAndGet().also { error("down") } } }

        val result = onDemand.ensure(snapshot::get) {
            runs.incrementAndGet()
            snapshot.set("ready")
        }

        result shouldBe "ready"
        runs.get() shouldBe 2
        currentTime shouldBe 10_000
    }

    @Test
    fun `callers arriving together during the cool-down trigger one refresh after a single wait`() = runTest {
        val onDemand = OnDemandRefresh(Mutex(), coolDown = 10.seconds, timeSource = testTimeSource)
        runCatching { onDemand.ensure(snapshot::get) { runs.incrementAndGet().also { error("down") } } }

        val outcomes = (1..4)
            .map {
                async {
                    runCatching {
                        onDemand.ensure(snapshot::get) {
                            runs.incrementAndGet()
                            error("still down")
                        }
                    }.exceptionOrNull()
                }
            }.awaitAll()

        runs.get() shouldBe 2
        outcomes.all { it is ClusterNotReadyException } shouldBe true
        currentTime shouldBe 10_000
    }

    @Test
    fun `cancelling the caller during a refresh propagates and leaves no cool-down`() = runBlocking {
        val onDemand = OnDemandRefresh(Mutex(), coolDown = 10.seconds)
        val caller = launch {
            onDemand.ensure(snapshot::get) {
                runs.incrementAndGet()
                awaitCancellation()
            }
        }
        while (runs.get() == 0) delay(5)

        caller.cancelAndJoin()
        onDemand.ensure(snapshot::get) {
            runs.incrementAndGet()
            snapshot.set("ready")
        } shouldBe "ready"

        caller.isCancelled shouldBe true
        runs.get() shouldBe 2
    }
}
