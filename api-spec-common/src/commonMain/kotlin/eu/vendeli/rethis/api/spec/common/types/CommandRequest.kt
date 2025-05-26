package eu.vendeli.rethis.api.spec.common.types

import eu.vendeli.rethis.api.spec.common.utils.CRC16
import io.ktor.util.reflect.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

data class CommandRequest<K>(
    val buffer: Buffer,
    val operation: RedisOperation,
    val typeInfo: TypeInfo,
    val isBlocking: Boolean = false,
) {
    private var keys: Lazy<K>? = null
    var routingHint: RoutingHint = RoutingHint.KEYLESS
    val computedKey: Int?
        get() = keys?.value?.let { v ->
            if (v.isCollection()) {
                v.haveAsList().map { // todo remove toList in array case
                    CRC16.lookup(it.toString().toByteArray())
                }.toSet().singleOrNull() ?: throw IllegalArgumentException("Cross slot operations are not supported")
            } else {
                CRC16.lookup(v.toString().toByteArray())
            } % 16384
        }

    fun withKey(key: K): CommandRequest<K> {
        keys = lazy { key }
        routingHint = if (key.isCollection()) {
            RoutingHint.MULTI_KEY
        } else {
            RoutingHint.SINGLE_KEY
        }
        return this
    }

    private inline fun K.isCollection() = this is List<*> || this is Array<*>
    private fun K.haveAsList() = when (this) {
        is Array<*> -> toList()
        is List<*> -> this
        else -> throw IllegalArgumentException("Cannot convert $this to List")
    }

    companion object {
        fun keyless(
            buffer: Buffer,
            operation: RedisOperation,
            typeInfo: TypeInfo,
            isBlocking: Boolean,
        ): CommandRequest<Nothing> = CommandRequest(
            buffer = buffer,
            operation = operation,
            typeInfo = typeInfo,
            isBlocking = isBlocking
        )
    }
}
