package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.utils.NameNormalizer

internal interface SpecNodeVisitor {
    fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext)
    fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext)
    fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext)
    fun visitBlock(node: SpecNode.Block, ctx: ValidationContext)

    fun String.specTypeNormalization(): String = when (lowercase()) {
        "key", "pattern" -> "string"
        "integer", "int" -> "long"
        "unix-time" -> "instant"
        else -> this
    }

    fun String.libTypeNormalization(): String = when (lowercase()) {
        "duration" -> "long"
        else -> this
    }

    fun KSType.isCollection(): Boolean {
        val q = declaration.qualifiedName?.asString() ?: return false
        return q.startsWith("kotlin.collections.") || q.endsWith(".Array")
    }
}


internal sealed class SpecNode(
    val name: String,
    val optional: Boolean,
    val multiple: Boolean,
    open val parentNode: SpecNode? = null,
) {
    abstract val token: String?
    var processed: Boolean = false

    abstract fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext)

    data class Simple(
        val type: String,
        val specName: String,
        val isOptional: Boolean,
        val isMultiple: Boolean,
        override val token: String? = null,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, isOptional, isMultiple) {
        val normalizedName get() = NameNormalizer.normalizeParam(specName)

        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitSimple(this, ctx)
    }

    data class PureToken(
        val specName: String,
        override val token: String,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, optional = false, multiple = false) {
        val normalizedName get() = NameNormalizer.normalizeParam(specName)

        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitPureToken(this, ctx)
    }

    data class OneOf(
        val specName: String,
        val options: List<SpecNode>,
        val isOptional: Boolean,
        val isMultiple: Boolean,
        override val token: String? = null,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, isOptional, isMultiple) {
        val normalizedName get() = NameNormalizer.normalizeParam(specName)

        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitOneOf(this, ctx)
    }

    data class Block(
        val specName: String,
        override val token: String?,
        val children: List<SpecNode>,
        val isOptional: Boolean,
        val isMultiple: Boolean,
        override val parentNode: SpecNode? = null,
    ) : SpecNode(specName, isOptional, isMultiple) {
        val normalizedName get() = NameNormalizer.normalizeParam(specName)

        override fun accept(visitor: SpecNodeVisitor, ctx: ValidationContext) =
            visitor.visitBlock(this, ctx)
    }
}

