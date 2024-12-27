package eu.vendeli.rethis.tests

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.get
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.core.BulkString
import eu.vendeli.rethis.types.core.PlainString
import eu.vendeli.rethis.types.core.RespVer
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PipeliningTest : ReThisTestCtx() {
    @Test
    fun `pipelining test`(): Unit = runTest {
        val v2Client = ReThis(redis.host, redis.firstMappedPort, protocol = RespVer.V2) {
            pool {
                poolSize = 1
            }
        }
        v2Client
            .pipeline {
                set("test1", "testv1")
                get("test1")
            }.run {
                this shouldHaveSize 2
                first() shouldBe PlainString("OK")
                last() shouldBe BulkString("testv1")
            }
    }

    @Test
    fun `pipelining + transaction test`(): Unit = runTest {
        client
            .transaction {
                client.pipeline {
                    set("test1", "testv1")
                    get("test1")
                }
            }.run {
                this shouldHaveSize 2
                first() shouldBe PlainString("OK")
                last() shouldBe BulkString("testv1")
            }
    }

    @Test
    fun `transaction + pipeline test`(): Unit = runTest {
        client
            .pipeline {
                client.transaction {
                    set("test1", "testv1")
                    get("test1")
                }

                set("test2", "value2")
                get("test2")
            }.run {
                this shouldHaveSize 4
                first() shouldBe PlainString("OK")
                get(1) shouldBe BulkString("testv1")
                get(2) shouldBe PlainString("OK")
                last() shouldBe BulkString("value2")
            }
    }

    @Test
    fun `nested pipeline test`(): Unit = runTest {
        client
            .pipeline {
                set("test1", "testv1")
                get("test1")

                pipeline {
                    set("test2", "value2")
                    get("test2")
                }
            }.run {
                this shouldHaveSize 4
                first() shouldBe PlainString("OK")
                get(1) shouldBe BulkString("testv1")
                get(2) shouldBe PlainString("OK")
                last() shouldBe BulkString("value2")
            }
    }

    @Test
    fun `nested transaction test`(): Unit = runTest {
        client
            .transaction {
                set("test1", "testv1")
                client.transaction {
                    get("test1")
                }
            }.run {
                this shouldHaveSize 2
                first() shouldBe PlainString("OK")
                last() shouldBe BulkString("testv1")
            }
    }
}
