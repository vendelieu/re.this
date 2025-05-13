package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

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

    fun KSClassDeclaration.isSealed(): Boolean =
        Modifier.SEALED in modifiers
}
