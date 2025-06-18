package eu.vendeli.rethis.api.spec.common.types

import io.ktor.util.reflect.*
import kotlinx.io.Buffer

data class CommandRequest(
    val buffer: Buffer,
    val operation: RedisOperation,
    val typeInfo: TypeInfo,
    val isBlocking: Boolean = false,
) {
    private var _key: Int? = null
    val computedSlot: Int? get() = _key

    fun withSlot(key: Int): CommandRequest {
        _key = key
        return this
    }
}
