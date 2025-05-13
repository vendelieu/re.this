package eu.vendeli.rethis.api.spec.common.annotations

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RedisMeta {
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Ignore

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class OrderPriority(val priority: Int = 0)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CustomCodec(val decoder: KClass<out ResponseDecoder<*>> = Nothing::class, val encoder: KClass<*> = Unit::class)

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class WithSizeParam(val name: String)
}
