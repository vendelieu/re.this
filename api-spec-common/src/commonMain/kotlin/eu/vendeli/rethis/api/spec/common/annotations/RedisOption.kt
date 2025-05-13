package eu.vendeli.rethis.api.spec.common.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RedisOption {
    @Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Name(val name: String)

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class SkipName
}
