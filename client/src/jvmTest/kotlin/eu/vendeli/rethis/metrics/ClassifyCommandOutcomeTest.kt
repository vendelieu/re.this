package eu.vendeli.rethis.metrics

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.ClusterException
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.shared.types.RedirectMovedException
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.types.RespUnexpectedEOF
import eu.vendeli.rethis.topology.classifyCommandOutcome
import eu.vendeli.rethis.types.interfaces.CommandOutcome
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import io.kotest.matchers.shouldBe
import kotlinx.io.IOException

@OptIn(ExperimentalReThisMetricsApi::class)
class ClassifyCommandOutcomeTest : TestCtx() {
    @Test
    fun `redis side exceptions map to ERROR_REDIS`() {
        classifyCommandOutcome(RedirectMovedException("MOVED", 1, "h", 1)) shouldBe CommandOutcome.ERROR_REDIS
        classifyCommandOutcome(ClusterException("cluster")) shouldBe CommandOutcome.ERROR_REDIS
        classifyCommandOutcome(ResponseParsingException("parse")) shouldBe CommandOutcome.ERROR_REDIS
        classifyCommandOutcome(ReThisException("plain")) shouldBe CommandOutcome.ERROR_REDIS
    }

    @Test
    fun `timeout stays ERROR_TIMEOUT although it is a ReThisException`() {
        classifyCommandOutcome(CommandTimeoutException("t")) shouldBe CommandOutcome.ERROR_TIMEOUT
    }

    @Test
    fun `transport failures map to ERROR_IO`() {
        classifyCommandOutcome(IOException("io")) shouldBe CommandOutcome.ERROR_IO
        classifyCommandOutcome(RespUnexpectedEOF()) shouldBe CommandOutcome.ERROR_IO
    }

    @Test
    fun `unrelated exceptions map to ERROR_OTHER`() {
        classifyCommandOutcome(IllegalStateException("x")) shouldBe CommandOutcome.ERROR_OTHER
    }
}
