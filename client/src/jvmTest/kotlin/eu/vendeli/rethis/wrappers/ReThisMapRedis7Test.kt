package eu.vendeli.rethis.wrappers

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.utility.DockerImageName

class ReThisMapRedis7Test : AnnotationSpec() {
    @Test
    fun `remove works on a server without HGETDEL`() {
        val container = RedisContainer(DockerImageName.parse("redis:7.4.0"))
        try {
            container.start()
            listOf(RespVer.V2, RespVer.V3).forEach { protocol ->
                val client = ReThis(container.host, container.firstMappedPort, protocol)
                try {
                    val map = client.Hash("redis7Map$protocol")
                    map["k"] = "v"
                    map.remove("k") shouldBe "v"
                    map.containsKey("k") shouldBe false
                    map.remove("k") shouldBe null
                } finally {
                    client.close()
                }
            }
        } finally {
            runCatching { container.stop() }
        }
    }
}
