package eu.vendeli.rethis.shared.annotations

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.TimeUnit
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

    @Target(AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class OutgoingTimeUnit(val unit: TimeUnit)

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class SkipCommand

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Default(val value: String)
}
