package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.list.blPop
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.shared.types.ClusterException
import eu.vendeli.rethis.shared.utils.CRC16
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ClusterAskConnectionReuseTest : TestCtx() {
    private val firstRange = 0..8191
    private val secondRange = 8192..16383
    private val firstKey = generateSequence(0) { it + 1 }.map { "k$it" }.first {
        CRC16.slot(it.toByteArray()) in
            firstRange
    }

    @Test
    fun `a rejected ASKING keeps the clean connection of the ask target`() {
        ScriptedClusterNode().use { owner ->
            ScriptedClusterNode().use { target ->
                val slotsReply = clusterSlotsReply(
                    SlotBlock(firstRange, owner.address),
                    SlotBlock(secondRange, target.address),
                )
                val slot = CRC16.slot(firstKey.toByteArray())
                owner.slotsReply = slotsReply
                owner.override = { if (it.first() == "GET") "-ASK $slot ${target.address.hostPort()}\r\n" else null }
                target.override = { if (it.first() == "ASKING") "-ERR no ask\r\n" else null }

                val client = ReThis.cluster(listOf(owner.address)) {
                    movedBackoffPeriod = 10.milliseconds
                    pool { minIdleConnections = 0 }
                }
                try {
                    runBlocking {
                        withTimeout(20.seconds) {
                            repeat(3) {
                                shouldThrow<ClusterException> { client.get(firstKey) }.message shouldContain "ASKING"
                            }
                        }
                    }
                    target.commandCount("ASKING") shouldBe 3
                    target.acceptedConnections shouldBe 1
                } finally {
                    client.close()
                }
            }
        }
    }

    @Test
    fun `an ask replay of a blocking command is not cut off by the command timeout`() {
        ScriptedClusterNode().use { owner ->
            ScriptedClusterNode().use { target ->
                val slotsReply = clusterSlotsReply(
                    SlotBlock(firstRange, owner.address),
                    SlotBlock(secondRange, target.address),
                )
                val slot = CRC16.slot(firstKey.toByteArray())
                owner.slotsReply = slotsReply
                owner.override = {
                    if (it.first() == "BLPOP") "-ASK $slot ${target.address.hostPort()}\r\n" else null
                }
                target.override = {
                    if (it.first() == "BLPOP") "*2\r\n\$${firstKey.length}\r\n$firstKey\r\n\$1\r\nv\r\n" else null
                }
                target.delayFor = { if (it.first() == "BLPOP") 900 else 0 }

                val client = ReThis.cluster(listOf(owner.address)) {
                    commandTimeout = 300.milliseconds
                    retry { times = 0 }
                    pool { minIdleConnections = 0 }
                }
                try {
                    runBlocking {
                        withTimeout(20.seconds) { client.blPop(firstKey, timeout = 1.0) } shouldBe
                            PopResult(firstKey, "v")
                    }
                } finally {
                    client.close()
                }
            }
        }
    }
}
