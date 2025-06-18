package eu.vendeli.rethis.api.spec.common.annotations

@Target()
annotation class RedisOption {
    @Repeatable
    @Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
    annotation class Token(val name: String)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
    annotation class Name(val name: String)
}
