package eu.vendeli.rethis.annotations

@DslMarker
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE,
    AnnotationTarget.VALUE_PARAMETER,
)
annotation class ReThisDSL
