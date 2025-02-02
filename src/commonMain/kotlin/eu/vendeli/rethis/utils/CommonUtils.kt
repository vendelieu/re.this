@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.utils.response.parseResponse
import eu.vendeli.rethis.utils.response.readResponseWrapped
import io.ktor.util.reflect.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

fun RType.isOk() = unwrap<String>() == "OK"

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.cast(): T = this as T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T : Any> Any.cast(clazz: KClass<T>): T = this as T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T> Any.safeCast(): T? = this as? T

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T : Any> Any.safeCast(clazz: KClass<T>): T? =
    if (this::class == clazz) this as T else null

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <T : Any> Any.safeCast(typeInfo: TypeInfo): T? =
    if (typeInfo.type.isInstance(this)) this as T else null

@Suppress("NOTHING_TO_INLINE")
private inline fun String?.isEqTo(other: String) = if (this != null) {
    compareTo(other.lowercase()) == 0
} else {
    false
}

@ReThisInternal
@JvmName("executeSimple")
suspend inline fun <reified T : Any> ReThis.execute(
    payload: List<Argument>,
): T? = execute(payload, typeInfo<T>())

@ReThisInternal
@JvmName("executeList")
suspend inline fun <reified T : Any> ReThis.execute(
    payload: List<Argument>,
    isCollectionResponse: Boolean = false,
): List<T>? = execute(payload, typeInfo<T>(), isCollectionResponse)

@ReThisInternal
@JvmName("executeMap")
suspend inline fun <reified K : Any, reified V : Any> ReThis.execute(
    payload: List<Argument>,
): Map<K, V?>? = execute(payload, typeInfo<K>(), typeInfo<V>())

internal suspend inline fun <reified T : CoroutineContext.Element> takeFromCoCtx(element: CoroutineContext.Key<T>): T? =
    currentCoroutineContext()[element]

internal suspend inline fun ReThis.registerSubscription(
    regCommand: String,
    unRegCommand: String,
    target: String,
    messageMarker: String,
    handler: SubscriptionHandler,
) {
    val connection = connectionPool.createConn()
    val handlerJob = coScope.launch(CoLocalConn(connection)) {
        val conn = currentCoroutineContext()[CoLocalConn]!!.connection
        try {
            conn.sendRequest(listOf(regCommand.toArgument(), target.toArgument()), cfg.charset)

            while (isActive) {
                conn.input.awaitContent()
                val msg = conn.input.parseResponse().readResponseWrapped(cfg.charset)
                val input = if (msg is Push) msg.value else msg.safeCast<RArray>()?.value
                logger.debug("Handling event in $target channel subscription")

                val inputType = input?.firstOrNull()?.value?.safeCast<String>()
                when {
                    inputType.isEqTo(regCommand) -> {
                        val targetCh = input?.getOrNull(1)?.unwrap<String>() ?: target
                        val subscribers = input?.lastOrNull()?.unwrap<Long>() ?: 0L
                        subscriptions.eventHandler?.onSubscribe(targetCh, subscribers)
                    }

                    inputType.isEqTo(unRegCommand) -> {
                        val targetCh = input?.getOrNull(1)?.unwrap<String>() ?: target
                        val subscribers = input?.lastOrNull()?.unwrap<Long>() ?: 0L
                        subscriptions.eventHandler?.onUnsubscribe(targetCh, subscribers)
                        subscriptions.unsubscribe(targetCh)
                    }

                    inputType == messageMarker && input.getOrNull(1)?.value == target -> {
                        handler.onMessage(this@registerSubscription, input.last().unwrap<String>() ?: "")
                    }
                }

                delay(1)
            }
        } catch (e: Exception) {
            logger.error("Caught exception in $target channel handler")
            subscriptions.eventHandler?.onException(target, e)
        } finally {
            conn.sendRequest(listOf(unRegCommand.toArgument(), target.toArgument()), cfg.charset)
            subscriptions.unsubscribe(target)
            connection.socket.close()
        }
    }

    subscriptions.jobs[target] = handlerJob
}
