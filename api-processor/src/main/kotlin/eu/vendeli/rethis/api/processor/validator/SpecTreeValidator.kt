package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import eu.vendeli.rethis.api.processor.type.SpecNode
import eu.vendeli.rethis.api.processor.type.SpecNodeVisitor
import eu.vendeli.rethis.api.processor.type.ValidationContext

internal class SpecTreeValidator(
    private val simpleVal: SimpleValidator = SimpleValidator,
    private val tokenVal: PureTokenValidator = PureTokenValidator,
    private val oneOfVal: OneOfValidator = OneOfValidator,
    private val blockVal: BlockValidator = BlockValidator,
) {
    fun validateAll(ctx: ValidationContext) {
        // 1. key checks (as before)
        // 2. walk tree
        ctx.specTree.forEach { it.accept(asVisitor(), ctx) }
        // 3. order checks
        validateParameterOrder(ctx)
        ctx.func.parameters
            .filter { it.isVararg }
            .forEach { validateVarargOptionOrder(it, ctx) }
    }

    private fun asVisitor(): SpecNodeVisitor = object : SpecNodeVisitor {
        override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) = simpleVal.visitSimple(node, ctx)
        override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) =
            tokenVal.visitPureToken(node, ctx)

        override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) = oneOfVal.visitOneOf(node, ctx)
        override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) = blockVal.visitBlock(node, ctx)
    }

    private fun validateParameterOrder(ctx: ValidationContext) {
        // todo check correctness
        val priorities = ctx.func.parameters.mapIndexedNotNull { idx, p ->
            val name = p.name?.asString() ?: return@mapIndexedNotNull null
            val explicit = p.annotations
                .firstOrNull { it.shortName.asString() == "OrderPriority" }
                ?.arguments?.firstOrNull { it.name?.asString() == "priority" }
                ?.value as? Int
            name to (explicit ?: idx)
        }.toMap()
        val names = ctx.func.parameters.mapNotNull { it.name?.asString() }
        names.zipWithNext().forEach { (a, b) ->
            val pa = priorities[a] ?: Int.MAX_VALUE
            val pb = priorities[b] ?: Int.MAX_VALUE
            if (pb < pa) ctx.errors += "Order mismatch: '$b'(=$pb) should come before '$a'(=$pa)'"
        }
    }

    private fun validateVarargOptionOrder(
        param: KSValueParameter,
        ctx: ValidationContext,
    ) {
        val paramName = param.name?.asString() ?: return
        // Resolve the vararg element type's declaration
        val elementType = param.type.resolve()
            .arguments
            .firstOrNull()
            ?.type
            ?.resolve()
            ?.declaration as? KSClassDeclaration
            ?: return

        // Gather all direct subclasses (sealed-class options)
        val subclasses = elementType.declarations
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS }

        // Map each subclass to its priority: explicit @OrderPriority or fallback to declaration order
        val priorities = subclasses.mapIndexed { idx, subDecl ->
            val ann = subDecl.annotations
                .firstOrNull { it.shortName.asString() == "OrderPriority" }
            val explicit = ann
                ?.arguments
                ?.firstOrNull { it.name?.asString() == "priority" }
                ?.value as? Int
            explicit ?: (idx + 1)
        }

        // Check for duplicates
        val duplicatePriorities = priorities
            .groupBy { it }
            .filter { it.value.size > 1 }
            .keys
        if (duplicatePriorities.isNotEmpty()) {
            ctx.reportError(
                "Vararg option '$paramName' has duplicate priorities: $duplicatePriorities",
            )
        }

        // Check for contiguous 1..N sequence
        val maxPriority = priorities.maxOrNull() ?: 0
        val expectedSet = (1..maxPriority).toSet()
        val actualSet = priorities.toSet()
        if (actualSet != expectedSet) {
            ctx.reportError(
                "Vararg option '$paramName' priorities are not contiguous: found $actualSet, expected $expectedSet",
            )
        }

        // Mark the vararg parameter processed
        ctx.markProcessed(paramName)
    }
}
