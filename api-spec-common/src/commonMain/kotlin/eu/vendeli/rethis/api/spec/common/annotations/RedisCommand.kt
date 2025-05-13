package eu.vendeli.rethis.api.spec.common.annotations

import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RedisCommand(
    val name: String,
    val operation: RedisOperation = RedisOperation.READ,
    val responseTypes: Array<RespCode>,
    val isBlocking: Boolean = false,
    val extensions: Array<KClass<*>> = [],
)
