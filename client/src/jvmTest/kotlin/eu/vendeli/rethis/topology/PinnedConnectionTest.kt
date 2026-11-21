package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.command.transaction.exec
import eu.vendeli.rethis.command.transaction.multi
import eu.vendeli.rethis.command.transaction.watch
import eu.vendeli.rethis.shared.types.PlainString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.seconds

class PinnedConnectionTest : ReThisTestCtx() {
    @Test
    suspend fun `watched key modified by another client aborts the transaction`() {
        val other = createClient()
        val isolated = createClient()
        isolated.set("pinAbortKey", "initial")

        val result = isolated.withConnection("pinAbortKey") {
            watch("pinAbortKey")
            get("pinAbortKey") shouldBe "initial"
            other.set("pinAbortKey", "changed")
            multi()
            set("pinAbortKey", "mine")
            exec()
        }

        result shouldBe null
        isolated.get("pinAbortKey") shouldBe "changed"
    }

    @Test
    suspend fun `untouched watched key commits the transaction`() {
        val isolated = createClient()
        isolated.set("pinCommitKey", "initial")

        val result = isolated.withConnection("pinCommitKey") {
            watch("pinCommitKey")
            get("pinCommitKey") shouldBe "initial"
            transaction { set("pinCommitKey", "mine") }
        }

        result shouldBe listOf(PlainString("OK"))
        isolated.get("pinCommitKey") shouldBe "mine"
    }

    @Test
    suspend fun `watch state does not leak to the next scope`() {
        val other = createClient()
        val isolated = createClient { pool { maxIdleConnections = 1 } }

        isolated.withConnection { watch("pinLeakKey") }
        other.set("pinLeakKey", "changed")

        isolated.withConnection {
            transaction { set("pinLeakKey", "mine") } shouldBe listOf(PlainString("OK"))
        }
    }

    @Test
    suspend fun `nested scope reuses the pinned connection`() {
        val other = createClient()
        val isolated = createClient()

        val result = isolated.withConnection("pinNestedKey") {
            watch("pinNestedKey")
            withConnection("pinNestedKey") { other.set("pinNestedKey", "changed") }
            transaction { set("pinNestedKey", "mine") }
        }

        result shouldBe null
    }

    @Test
    suspend fun `connection is released after normal completion, failure and cancellation`() {
        val isolated = createClient {
            maxConnections = 1
            connectionAcquireTimeout = 3.seconds
        }

        isolated.withConnection { set("pinReleaseKey", "1") }
        isolated.get("pinReleaseKey") shouldBe "1"

        shouldThrow<IllegalStateException> { isolated.withConnection { error("boom") } }
        isolated.get("pinReleaseKey") shouldBe "1"

        val started = CompletableDeferred<Unit>()
        val job = kotlinx.coroutines.CoroutineScope(kotlin.coroutines.coroutineContext).async {
            isolated.withConnection {
                set("pinReleaseKey", "2")
                started.complete(Unit)
                awaitCancellation()
            }
        }
        started.await()
        job.cancel()
        shouldThrow<CancellationException> { job.await() }
        yield()
        isolated.get("pinReleaseKey") shouldBe "2"
    }
}
