package eu.vendeli.rethis.annotations

/**
 * The API marked with this annotation is internal, and it is not intended to be used outside ReThis.
 * It could be modified or removed without any notice. Using it outside could cause undefined behavior and/or
 * any unexpected effects.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API is internal in ReThis. " +
        "It could be removed or changed without notice. Use may break internal logic, use with caution.",
)
@MustBeDocumented
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
annotation class ReThisInternal
