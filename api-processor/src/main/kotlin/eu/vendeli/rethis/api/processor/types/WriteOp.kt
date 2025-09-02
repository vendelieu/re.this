package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.context.CodeGenContext.BlockType
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.shared.annotations.RedisOption

internal sealed class WriteOp {
    abstract val node: EnrichedNode

    data class DirectCall(
        override val node: EnrichedNode,
        val slotBlock: CodeGenContext.(Boolean) -> Unit,
        val encodeBlock: CodeGenContext.(Boolean) -> Unit,
    ) : WriteOp()

    data class WrappedCall(
        override val node: EnrichedNode,
        val props: MutableSet<WriteOpProps>,
        val inner: List<WriteOp>,
        val tokens: MutableList<EnrichedTreeAttr.Token>,
    ) : WriteOp()

    data class Dispatch(
        override val node: EnrichedNode,
        val branches: Map<KSType, List<WriteOp>>,
    ) : WriteOp()
}

internal enum class WriteOpProps {
    NULLABLE, SINGLE_TOKEN, WITH_SIZE, COLLECTION, MULTIPLE_TOKEN;
}

internal fun WriteOp.filterWithoutKey(): WriteOp? = when (this) {
    is WriteOp.DirectCall -> if (node.attr.contains(EnrichedTreeAttr.Key)) this else null
    is WriteOp.WrappedCall -> {
        val filteredInner = inner.mapNotNull { it.filterWithoutKey() }
        if (filteredInner.isEmpty()) null else copy(inner = filteredInner)
    }

    is WriteOp.Dispatch -> {
        val filteredBranches = branches.mapValues { (_, ops) ->
            ops.mapNotNull { it.filterWithoutKey() }
        }.filterValues { it.isNotEmpty() }
        if (filteredBranches.isEmpty()) null else copy(branches = filteredBranches)
    }
}

@OptIn(KspExperimental::class)
internal fun WriteOp.emitOp(encode: Boolean, complex: Boolean = false) {
    val ctx = context[CodeGenContext] ?: run {
        context.logger.error("CodeGenContext not found")
        return
    }

    when (this) {
        is WriteOp.DirectCall -> if (!encode && node.attr.contains(EnrichedTreeAttr.Key)) slotBlock(
            ctx,
            complex,
        ) else encodeBlock(ctx, complex)

        is WriteOp.Dispatch -> ctx.buildBlock(node.nameOrNull ?: ctx.pointer!!, BlockType.WHEN) {
            branches.forEach { (subType, branchOps) ->
                addImport(subType.declaration.qualifiedName!!.asString())
                ctx.builder.beginControlFlow("is ${subType.name} -> ")
                val processedTokens = mutableSetOf<String>()
                if (encode && !subType.declaration.isDataObject()) {
                    subType.declaration.getAnnotationsByType(RedisOption.Token::class).forEach {
                        if (context.currentCommand.haveVaryingSize) ctx.appendLine("size += 1")
                        ctx.appendLine("buffer.writeStringArg(\"${it.name}\", charset)")
                        processedTokens.add(it.name)
                    }
                }
                branchOps.forEach {
                    it.handleTokens(processedTokens)
                    it.emitOp(encode)
                }
                ctx.builder.endControlFlow()
            }
            if (!encode) ctx.builder.addStatement("else -> {}")
        }

        is WriteOp.WrappedCall -> {
            val isComplex = !node.type.declaration.isStdType() &&
                node.type.declaration.safeCast<KSClassDeclaration>()?.primaryConstructor?.parameters?.size?.let {
                    it > 0
                } == true

            fun handleWrapping(type: ArrayDeque<WriteOpProps>) {
                val action = {
                    when {
                        type.isNotEmpty() -> handleWrapping(type)
                        else -> {
                            if (isComplex && ctx.blockStack.lastOrNull() == null && node.nameOrNull != null) {
                                ctx.pointer = node.name
                            }
                            inner.forEach { it.emitOp(encode, isComplex) }
                        }
                    }
                }

                when (type.removeFirstOrNull()) {
                    WriteOpProps.NULLABLE -> ctx.buildBlock(node.name, BlockType.LET) { action() }

                    WriteOpProps.SINGLE_TOKEN -> {
                        if (encode) ctx.writeTokens(node, tokens) { t -> !t.multiple }
                        action()
                    }

                    WriteOpProps.WITH_SIZE -> {
                        if (encode) {
                            addImport("eu.vendeli.rethis.utils.writeIntArg")
                            if (context.currentCommand.haveVaryingSize) ctx.appendLine("size += 1")
                            ctx.appendLine("buffer.writeIntArg(%L.size, charset)", ctx.pointedParameter(node.name))
                        }
                        action()
                    }

                    WriteOpProps.COLLECTION -> ctx.buildBlock(node.name, BlockType.FOR) {
                        action()
                    }

                    WriteOpProps.MULTIPLE_TOKEN -> {
                        if (encode) ctx.writeTokens(node, tokens) { t -> t.multiple }
                        action()
                    }

                    else -> action()
                }
            }
            handleWrapping(ArrayDeque(props))
        }
    }
}

private fun WriteOp.handleTokens(processedTokens: Set<String>) {
    node.attr.removeIf { attr -> attr is EnrichedTreeAttr.Token && processedTokens.contains(attr.name) }
    if (this is WriteOp.WrappedCall) {
        tokens.removeIf { attr -> processedTokens.contains(attr.name) }
    }
}

internal fun CodeGenContext.writeTokens(
    node: EnrichedNode,
    tokens: List<EnrichedTreeAttr.Token>,
    filterPredicate: (EnrichedTreeAttr.Token) -> Boolean = { true },
) {
    val filteredTokens = tokens.filter(filterPredicate)
    val typeDecl = node.type.declaration
    if (typeDecl.isEnum() || typeDecl.isDataObject()) return

    val isBoolToken = typeDecl.isBool() && filteredTokens.isNotEmpty()
    if (isBoolToken) builder.beginControlFlow("if(${pointedParameter(node.name)})")

    if (filteredTokens.isNotEmpty()) addImport("eu.vendeli.rethis.utils.writeStringArg")
    filteredTokens.forEach {
        if (context.currentCommand.haveVaryingSize) appendLine("size += 1")
        appendLine("buffer.writeStringArg(\"${it.name}\", charset)")
        node.attr.remove(it)
    }

    if (isBoolToken) builder.endControlFlow()
}
