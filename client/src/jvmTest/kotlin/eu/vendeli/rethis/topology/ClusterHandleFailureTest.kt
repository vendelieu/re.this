package eu.vendeli.rethis.topology

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ClusterHandleFailureTest : TestCtx() {
    private val period = 5.seconds

    private val backOffErrors = listOf(
        IOException("connection reset"),
        CommandTimeoutException("timed out"),
        RespUnexpectedEOF(),
        RedirectMovedException("MOVED 1 h:1", 1, "h", 1),
        RedirectUnstableException("TRYAGAIN", "TRYAGAIN"),
        DownUnboundSlotException("CLUSTERDOWN Hash slot not served", "CLUSTERDOWN"),
        DownReadOnlyStateException("CLUSTERDOWN The cluster is down and only accepts read commands", "CLUSTERDOWN"),
    )

    private val permanentErrors = listOf(
        ReThisException("WRONGTYPE Operation against a key holding the wrong kind of value"),
        ResponseParsingException("bad frame"),
        ClusterException("No master for slot 9000"),
        KeyAbsentException("no key"),
        CrossSlotOperationException("CROSSSLOT"),
    )

    @Test
    fun `connection level and cluster state errors back off`() {
        backOffErrors.forEach { (it.shouldBackOff()) shouldBe true }
    }

    @Test
    fun `permanent errors do not back off`() {
        permanentErrors.forEach { (it.shouldBackOff()) shouldBe false }
    }

    @Test
    fun `permanent errors are rethrown without waiting`() = runTest {
        permanentErrors.forEach {
            val thrown = shouldThrow<Throwable> { failAfterBackoff(it, period) }

            (thrown === it) shouldBe true
            currentTime shouldBe 0
        }
    }

    @Test
    fun `back off errors wait for the configured period before rethrowing`() {
        backOffErrors.forEach {
            runTest {
                val thrown = shouldThrow<Throwable> { failAfterBackoff(it, period) }

                (thrown === it) shouldBe true
                currentTime shouldBe period.inWholeMilliseconds
            }
        }
    }
}
