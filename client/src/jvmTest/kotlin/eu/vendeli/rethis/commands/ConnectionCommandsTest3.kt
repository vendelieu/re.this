package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.connection.clientGetRedir
import eu.vendeli.rethis.command.connection.clientNoEvict
import eu.vendeli.rethis.command.connection.clientNoTouch
import eu.vendeli.rethis.command.connection.clientSetInfo
import eu.vendeli.rethis.command.connection.clientTrackingInfo
import eu.vendeli.rethis.command.connection.clientUnpause
import eu.vendeli.rethis.command.connection.echo
import eu.vendeli.rethis.command.connection.reset
import eu.vendeli.rethis.shared.request.connection.ClientNoEvictMode
import eu.vendeli.rethis.shared.request.connection.ClientNoTouchMode
import eu.vendeli.rethis.shared.request.connection.ClientSetInfoAttribute
import eu.vendeli.rethis.shared.types.RType
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ConnectionCommandsTest3 : ReThisTestCtx() {
    @Test
    suspend fun `test ECHO command`() {
        client.echo("hello-world") shouldBe "hello-world"
    }

    @Test
    suspend fun `test RESET command`() {
        client.reset() shouldBe "RESET"
    }

    @Test
    suspend fun `test CLIENT GETREDIR command`() {
        client.clientGetRedir() shouldBe -1L
    }

    @Test
    suspend fun `test CLIENT UNPAUSE command`() {
        client.clientUnpause().shouldBeTrue()
    }

    @Test
    suspend fun `test CLIENT NO-EVICT command`() {
        client.clientNoEvict(ClientNoEvictMode.ON).shouldBeTrue()
        client.clientNoEvict(ClientNoEvictMode.OFF).shouldBeTrue()
    }

    @Test
    suspend fun `test CLIENT NO-TOUCH command`() {
        client.clientNoTouch(ClientNoTouchMode.ON).shouldBeTrue()
        client.clientNoTouch(ClientNoTouchMode.OFF).shouldBeTrue()
    }

    @Test
    suspend fun `test CLIENT SETINFO command with LIB-NAME`() {
        client.clientSetInfo(ClientSetInfoAttribute.LibName("rethis-tests")).shouldBeTrue()
    }

    @Test
    suspend fun `test CLIENT SETINFO command with LIB-VER`() {
        client.clientSetInfo(ClientSetInfoAttribute.LibVer("0.0.0-test")).shouldBeTrue()
    }

    @Test
    suspend fun `test CLIENT TRACKINGINFO command`() {
        client.clientTrackingInfo() shouldNotBe RType.Null
    }
}
