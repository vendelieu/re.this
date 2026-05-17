package eu.vendeli.rethis.api.processor.context

/**
 * A typed access path into a generated codec's encode function: the value or property reference
 * the emitter is currently writing about.
 *
 * Replaces the previous string-typed `parentPointer: String?` breadcrumb threaded through
 * [eu.vendeli.rethis.api.processor.types.WriteOp.emitOp] and the magic `CodeGenContext.pointer`
 * variable. Each variant corresponds to a distinct emission context, so consumers no longer have
 * to disambiguate `""`, `null`, a field name, a binding name, or a dotted path at runtime.
 */
internal sealed interface Access {
    /** No enclosing scope — emission is at the encode function's top level. */
    data object Top : Access

    /** A direct parameter or local in scope, e.g. `key`, `streams`, `subcommand`. */
    data class Field(val name: String) : Access

    /**
     * A reference to a lambda-bound value via a [Binding] handle (e.g. `it0`, `it1`).
     *
     * Holds the handle by identity so [render] can mark it as used at the moment the access is
     * actually emitted as text — this is what allows the surrounding scope to decide between
     * `it0` and `_` for the lambda parameter without inspecting rendered output.
     */
    class Bound internal constructor(internal val binding: Binding) : Access {
        override fun equals(other: Any?): Boolean = other is Bound && other.binding === binding
        override fun hashCode(): Int = System.identityHashCode(binding)
        override fun toString(): String = "Access.Bound(${binding.name})"
    }

    /** A dotted path: `parent.field`, e.g. `it0.member`, `streams.key`. */
    data class Qualified(val parent: Access, val field: String) : Access
}

/**
 * Render the access as the source-level expression that references its target value.
 *
 * Walking through a [Access.Bound] node *marks the underlying binding as used* — this is the
 * single point where binding-usage is registered. Anything that ends up as text in the generated
 * codec went through this function; anything that didn't (e.g. an [Access] value built but never
 * emitted) doesn't count as a use.
 */
internal fun Access.render(): String = when (this) {
    Access.Top -> ""
    is Access.Field -> name
    is Access.Bound -> {
        binding.markUsed()
        binding.name
    }
    is Access.Qualified -> "${parent.render()}.$field"
}

/**
 * Extend the access path with a child [field].
 *
 * Special cases (preserve original generator output):
 * - `Top.qualify(x) == Field(x)`.
 * - When this is an [Access.Bound] whose handle's `sourceField` equals [field], return `this`
 *   unchanged — the binding already names that field's value (e.g. inside `keys.forEach { it0 -> }`,
 *   accessing field `keys` on a `List<String>` element resolves to `it0`, not `it0.keys`).
 * - Otherwise: `Qualified(this, field)`.
 */
internal fun Access.qualify(field: String): Access = when (this) {
    Access.Top -> Access.Field(field)
    is Access.Bound if binding.sourceField == field -> this
    else -> Access.Qualified(this, field)
}

/**
 * The "tail" field of an access path — the innermost named segment. For an [Access.Bound] this is
 * the binding's `sourceField` (what it iterates), for [Access.Field] it's the name, and for
 * [Access.Qualified] it's the trailing field. Used to derive a new binding's `sourceField` when
 * opening a nested scope: e.g., `keys.forEach { it0 -> it0.forEach { it1 -> ... } }` — the inner
 * binding inherits "keys" from its target.
 */
internal fun Access.tailField(): String = when (this) {
    Access.Top -> error("Top access has no tail field")
    is Access.Field -> name
    is Access.Bound -> binding.sourceField
    is Access.Qualified -> field
}
