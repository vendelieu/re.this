package eu.vendeli.rethis.api.spec.common.annotations

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck
import kotlin.reflect.KClass

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RedisMeta {
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CustomCodec(
        val decoder: KClass<out ResponseDecoder<*>> = ResponseDecoder::class,
        val encoder: KClass<*> = Unit::class,
    )

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class WithSizeParam(val name: String)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class IgnoreCheck(val check: Array<ValidityCheck>)

    @Target(AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class OutgoingTimeUnit(val unit: TimeUnit)

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class SkipCommand
}
