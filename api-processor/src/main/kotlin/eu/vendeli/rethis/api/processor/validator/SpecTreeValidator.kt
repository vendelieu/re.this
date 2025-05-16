package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import eu.vendeli.rethis.api.processor.type.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta

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
        validateOrder(ctx)
    }

    private fun asVisitor(): SpecNodeVisitor = object : SpecNodeVisitor {
        override fun visitSimple(node: SpecNode.Simple, ctx: ValidationContext) = simpleVal.visitSimple(node, ctx)
        override fun visitPureToken(node: SpecNode.PureToken, ctx: ValidationContext) =
            tokenVal.visitPureToken(node, ctx)

        override fun visitOneOf(node: SpecNode.OneOf, ctx: ValidationContext) = oneOfVal.visitOneOf(node, ctx)
        override fun visitBlock(node: SpecNode.Block, ctx: ValidationContext) = blockVal.visitBlock(node, ctx)
    }

    /**
     * Entry point: validates every argument‑list in the spec for the given command.
     */
    fun validateOrder(ctx: ValidationContext) {
        // 1) Look up the spec’s top‑level arguments:
        val topArgs = ctx.fullSpec.commands
            .getValue(ctx.currentCmd)
            .arguments
            ?: return

        // 2) Kick off the recursive checker:
        checkList(ctx, topArgs, path = listOf(ctx.currentCmd))
    }

    /**
     * Recursively checks that each sibling in `args` is ordered by priority.
     *
     * @param ctx   validation context (contains the Kotlin tree and error buffer)
     * @param args  JSON‑derived arguments at this level
     * @param path  breadcrumb path for error messages
     */
    @OptIn(KspExperimental::class)
    private fun checkList(
        ctx: ValidationContext,
        args: List<CommandArgument>,
        path: List<String>,
    ) {
        // Build a list of (displayPath, priority) for these siblings
        val siblings = args.mapIndexed { idx, arg ->
            // a) compute a human‑readable path
            val display = (path + arg.specName).joinToString(" → ")
            // b) find matching node in your Kotlin tree (either token or parameter)
            val node = arg.token
                ?.let { ctx.paramTree.findTokenByName(it) }
                ?: ctx.findParam(arg.normalizedName)
            // c) extract @OrderPriority if present
            val kPrio = node?.symbol?.getAnnotationsByType(RedisMeta.OrderPriority::class)?.firstOrNull()?.priority
            // d) fallback to spec index
            val prio = kPrio ?: idx
            display to prio
        }

        // 1) Check sortedness
        siblings.zipWithNext().forEach { (prev, next) ->
            val (pName, pPrio) = prev
            val (nName, nPrio) = next
            if (nPrio < pPrio) {
                ctx.reportError("Order mismatch: “$nName”(=$nPrio) should come after “$pName”(=$pPrio)")
            }
        }

        // 2) Check contiguity: e.g. 0..N‑1
        val prios = siblings.map { it.second }.toSet()
        val expected = (0 until siblings.size).toSet()
        if (prios != expected) {
            ctx.reportError("Non‑contiguous priorities: found $prios, expected $expected")
        }

        // 3) Recurse into any nested sub‑arguments
        args.forEach { arg ->
            if (arg.arguments.isNotEmpty()) {
                checkList(ctx, arg.arguments, path + arg.specName)
            }
        }
    }
}
