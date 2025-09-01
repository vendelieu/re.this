package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.utils.safeCast

internal class EnrichedNode(
    val parent: EnrichedNode? = null,
    val attr: MutableSet<EnrichedTreeAttr> = mutableSetOf(),
    val children: MutableSet<EnrichedNode> = mutableSetOf(),
) {
    val name by lazy { attr.filterIsInstance<EnrichedTreeAttr.Name>().single().name }
    val nameOrNull get() = attr.filterIsInstance<EnrichedTreeAttr.Name>().singleOrNull()?.name
    val tokens get() = attr.filterIsInstance<EnrichedTreeAttr.Token>()
    val ks by lazy { attr.filterIsInstance<EnrichedTreeAttr.Symbol>().single() }
    val rSpec get() = attr.filterIsInstance<EnrichedTreeAttr.RelatedRSpec>().firstOrNull()?.node
    val type by lazy {
        attr.filterIsInstance<EnrichedTreeAttr.Type>().singleOrNull()?.type
            ?: attr.filterIsInstance<EnrichedTreeAttr.Symbol>().single().symbol.safeCast<KSClassDeclaration>()
                ?.asStarProjectedType()!!
    }
}

internal sealed class EnrichedTreeAttr {
    data object Key : EnrichedTreeAttr()
    data object SizeParam : EnrichedTreeAttr()

    data class Multiple(val vararg: Boolean = false, val collection: Boolean = false) : EnrichedTreeAttr()
    data class Optional(
        val inherited: OptionalityType? = null,
        val local: OptionalityType? = null,
    ) : EnrichedTreeAttr()

    data class Token(val name: String, val multiple: Boolean) : EnrichedTreeAttr()

    data class Name(val name: String) : EnrichedTreeAttr()
    data class Type(val type: KSType) : EnrichedTreeAttr()
    data class Symbol(val symbol: KSAnnotated, val type: SymbolType) : EnrichedTreeAttr()

    data class RelatedRSpec(val node: RSpecNode) : EnrichedTreeAttr()
}

internal enum class OptionalityType {
    Vararg,
    Nullable
}

internal enum class SymbolType {
    ValueParam,
    Class,
    Function
}
