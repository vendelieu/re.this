package eu.vendeli.rethis.types

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.types.common.UrlAddress
import io.kotest.matchers.shouldBe
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.shouldNotBe
import io.ktor.network.sockets.*

class UrlAddressTest : TestCtx() {
    private fun UrlAddress.inet() = address.socket as InetSocketAddress

    @Test
    fun `user and password are read from the user-info`() {
        val url = UrlAddress("redis://alice:secret@h:6379/2")

        url.username shouldBe "alice"
        url.password shouldBe "secret"
        url.db shouldBe 2
        url.inet().hostname shouldBe "h"
        url.inet().port shouldBe 6379
    }

    @Test
    fun `empty user means password only`() {
        val url = UrlAddress("redis://:secret@h")

        url.username shouldBe null
        url.password shouldBe "secret"
    }

    @Test
    fun `user-info without colon is a password`() {
        val url = UrlAddress("redis://secret@h")

        url.username shouldBe null
        url.password shouldBe "secret"
    }

    @Test
    fun `no user-info yields no credentials`() {
        val url = UrlAddress("redis://h")

        url.username shouldBe null
        url.password shouldBe null
        url.authConfiguration() shouldBe null
    }

    @Test
    fun `empty user-info yields no credentials`() {
        UrlAddress("redis://@h").authConfiguration() shouldBe null
    }

    @Test
    fun `empty password with user is kept`() {
        val auth = UrlAddress("redis://alice:@h").authConfiguration()

        auth shouldNotBe null
        auth!!.username shouldBe "alice"
        String(auth.password) shouldBe ""
    }

    @Test
    fun `auth configuration maps user and password to the right fields`() {
        val auth = UrlAddress("redis://alice:secret@h:6379/2").authConfiguration()

        auth shouldNotBe null
        auth!!.username shouldBe "alice"
        String(auth.password) shouldBe "secret"
    }

    @Test
    fun `auth configuration of password-only url has no username`() {
        val auth = UrlAddress("redis://:secret@h").authConfiguration()

        auth shouldNotBe null
        auth!!.username shouldBe null
        String(auth.password) shouldBe "secret"
    }

    @Test
    fun `percent-encoded characters are decoded`() {
        val url = UrlAddress("redis://a%40b:p%3Aw%2Fd%40%C3%A9@h")

        url.username shouldBe "a@b"
        url.password shouldBe "p:w/d@é"
    }

    @Test
    fun `malformed percent sequences are kept literally`() {
        val url = UrlAddress("redis://u:100%@h")

        url.password shouldBe "100%"
        UrlAddress("redis://u:a%zzb%4@h").password shouldBe "a%zzb%4"
    }

    @Test
    fun `plus sign is not decoded as space`() {
        UrlAddress("redis://u:a+b@h").password shouldBe "a+b"
    }

    @Test
    fun `ipv6 host with credentials port and db`() {
        val url = UrlAddress("redis://u:p@[::1]:6380/3")

        // ktor resolves the literal, so assert on the resolved loopback address rather than the hostname string
        val resolved = (url.inet().toJavaAddress() as java.net.InetSocketAddress).address
        (resolved is java.net.Inet6Address && resolved.isLoopbackAddress) shouldBe true
        url.inet().port shouldBe 6380
        url.db shouldBe 3
        url.username shouldBe "u"
        url.password shouldBe "p"
    }

    @Test
    fun `defaults apply for host only url`() {
        val url = UrlAddress("redis://h")

        url.inet().port shouldBe 6379
        url.db shouldBe 0
    }

    @Test
    fun `at sign in query does not leak into credentials`() {
        val url = UrlAddress("redis://h:6379/0?x=a@b")

        url.password shouldBe null
        url.inet().hostname shouldBe "h"
    }

    @Test
    fun `toString and auth configuration do not expose the password`() {
        val url = UrlAddress("redis://alice:s3cr3t@h")

        url.toString().contains("s3cr3t") shouldBe false
        url.authConfiguration().toString().contains("s3cr3t") shouldBe false
    }

    @Test
    fun `query parameters are split by key and value`() {
        UrlAddress("redis://h:6379/0?timeout=5&tls=true").parameters shouldBe
            mapOf("timeout" to "5", "tls" to "true")
    }

    @Test
    fun `query flag without value has empty value`() {
        UrlAddress("redis://h?flag").parameters shouldBe mapOf("flag" to "")
    }

    @Test
    fun `query value keeps further equals signs`() {
        UrlAddress("redis://h?a=b=c").parameters shouldBe mapOf("a" to "b=c")
    }

    @Test
    fun `no query yields empty parameters`() {
        UrlAddress("redis://h:6379/0").parameters shouldBe emptyMap()
    }

    private fun invalid(url: String, vararg secrets: String): String {
        val message = shouldThrow<IllegalArgumentException> { UrlAddress(url) }.message.orEmpty()
        secrets.forEach { message shouldNotContain it }
        return message
    }

    @Test
    fun `unsupported scheme throws naming only the scheme`() {
        val message = invalid("http://alice:pwsecret@prod:6380", "pwsecret", "alice", "prod")

        message.contains("'http'") shouldBe true
    }

    @Test
    fun `url without scheme throws`() {
        invalid("alice:pwsecret@prod", "pwsecret", "alice", "prod")
        invalid("", "pwsecret")
    }

    @Test
    fun `raw at sign in password throws without echoing it`() {
        invalid("redis://u:pw@ss@host", "pw@ss", "ss@host")
    }

    @Test
    fun `invalid port throws naming the field only`() {
        invalid("redis://u:pwsecret@h:99999999999", "pwsecret", "99999999999").contains("port") shouldBe true
        invalid("redis://u:pwsecret@h:65536", "pwsecret", "65536")
        invalid("redis://u:pwsecret@h:0", "pwsecret")
    }

    @Test
    fun `non numeric port throws`() {
        invalid("redis://u:pwsecret@h:abc", "pwsecret", "abc")
    }

    @Test
    fun `invalid db throws naming the field only`() {
        invalid("redis://u:pwsecret@h:6379/99999999999", "pwsecret", "99999999999").contains("db") shouldBe true
        invalid("redis://u:pwsecret@h/abc", "pwsecret", "abc")
    }

    @Test
    fun `query keys and values are percent-decoded and empty entries ignored`() {
        UrlAddress("redis://h?a%20b=c%26d&&e=1&").parameters shouldBe mapOf("a b" to "c&d", "e" to "1")
    }
}
