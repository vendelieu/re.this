package eu.vendeli.rethis.shared.annotations

import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RedisCommand(
    val name: String,
    val operation: RedisOperation = RedisOperation.READ,
    val responseTypes: Array<RespCode>,
    val isBlocking: Boolean = false,
)
