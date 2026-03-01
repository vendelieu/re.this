package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.context.CodeGenContext.BlockType
import eu.vendeli.rethis.api.processor.context.CollectedTokens
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.shared.annotations.RedisOption

internal enum class EncodePhase {
    SIZE, WRITE, SLOT
}

internal sealed class WriteOp {
    abstract val node: EnrichedNode

    data class DirectCall(
        override val node: EnrichedNode,
        val slotBlock: CodeGenContext.(String?) -> Unit,
        val encodeBlock: CodeGenContext.(String?, EncodePhase) -> Unit,
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
internal fun WriteOp.emitOp(phase: EncodePhase, parentPointer: String? = null) {
    val ctx = context[CodeGenContext] ?: run {
        context.logger.error("CodeGenContext not found")
        return
    }

    when (this) {
        is WriteOp.DirectCall -> when (phase) {
            EncodePhase.SLOT -> if (node.attr.contains(EnrichedTreeAttr.Key)) slotBlock(ctx, parentPointer)
            EncodePhase.SIZE, EncodePhase.WRITE -> encodeBlock(ctx, parentPointer, phase)
        }

        is WriteOp.Dispatch -> ctx.buildBlock(node.nameOrNull ?: ctx.pointer!!, BlockType.WHEN, parentPointer) {
            branches.forEach { (subType, branchOps) ->
                addImport(subType.declaration.qualifiedName!!.asString())
                ctx.builder.beginControlFlow("is ${subType.name} -> ")
                val processedTokens = mutableSetOf<String>()
                if (phase != EncodePhase.SLOT) {
                    val tokens = subType.declaration.getAnnotationsByType(RedisOption.Token::class).toList()
                    tokens.forEach {
                        // Always collect tokens for RedisToken generation
                        if (phase == EncodePhase.WRITE) {
                            context[CollectedTokens]?.addToken(it.name)
                        }

                        // Write tokens for non-data-objects (data objects are handled in WriteUtils.kt)
                        if (!subType.declaration.isDataObject()) {
                            if (phase == EncodePhase.SIZE) {
                                if (context.currentCommand.haveVaryingSize) ctx.appendLine("argCount += 1")
                            } else if (phase == EncodePhase.WRITE) {
                                addImport("eu.vendeli.rethis.utils.writeBulkString")
                                val tokenProperty = tokenToRedisTokenPropertyName(it.name)
                                addImport("eu.vendeli.rethis.utils.RedisToken")
                                ctx.appendLine("buffer.writeBulkString(RedisToken.%L)", tokenProperty)
                            }
                        }
                        processedTokens.add(it.name)
                    }
                }
                branchOps.forEach {
                    it.handleTokens(processedTokens)
                    it.emitOp(phase, ctx.pointer)
                }
                ctx.builder.endControlFlow()
            }
            if (phase == EncodePhase.SLOT) ctx.builder.addStatement("else -> {}")
        }

        is WriteOp.WrappedCall -> {
            val isComplex = !node.type.declaration.isStdType() &&
                !node.type.declaration.isEnum() &&
                node.type.declaration.safeCast<KSClassDeclaration>()?.primaryConstructor?.parameters?.size?.let {
                    it > 0
                } == true

            val isCollection = props.contains(WriteOpProps.COLLECTION)
            
            // Get the node's name explicitly
            val nodeName = node.attr.filterIsInstance<EnrichedTreeAttr.Name>().singleOrNull()?.name
            
            // Calculate the pointer for children
            val childPointer = when {
                isComplex && !isCollection && nodeName != null -> nodeName
                else -> parentPointer
            }

            val thisNodePointer = parentPointer

            var collectionGuard = false
            fun handleWrapping(type: ArrayDeque<WriteOpProps>) {
                val action = {
                    when {
                        type.isNotEmpty() -> handleWrapping(type)
                        else -> {
                            inner.forEach { it.emitOp(phase, childPointer) }
                        }
                    }
                }

                when (type.removeFirstOrNull()) {
                    WriteOpProps.NULLABLE -> ctx.buildBlock(node.name, BlockType.LET, thisNodePointer) { action() }

                    WriteOpProps.SINGLE_TOKEN -> {
                        if (phase != EncodePhase.SLOT) {
                            if (type.contains(WriteOpProps.COLLECTION)) {
                                collectionGuard = true
                                val pName = if (thisNodePointer.isNullOrBlank()) node.name else "$thisNodePointer.${node.name}"
                                ctx.builder.beginControlFlow("if (%L.isNotEmpty())", pName)
                            }

                            ctx.writeTokens(node, tokens, phase) { t -> !t.multiple }

                            if (collectionGuard && !type.contains(WriteOpProps.WITH_SIZE)) {
                                collectionGuard = false
                                ctx.builder.endControlFlow()
                            }
                        }
                        action()
                    }

                    WriteOpProps.WITH_SIZE -> {
                        if (phase != EncodePhase.SLOT) {
                            val pName = if (thisNodePointer.isNullOrBlank()) node.name else "$thisNodePointer.${node.name}"
                            if (phase == EncodePhase.SIZE) {
                                ctx.appendLine("argCount += 1")
                            } else if (phase == EncodePhase.WRITE) {
                                addImport("eu.vendeli.rethis.utils.writeBulkString")
                                // Size is always dynamic, use toByteArray(charset)
                                ctx.appendLine("buffer.writeBulkString(%L.size.toString().toByteArray(charset))", pName)
                            }

                            if (collectionGuard) ctx.builder.endControlFlow()
                        }
                        action()
                    }

                    WriteOpProps.COLLECTION -> ctx.buildBlock(node.name, BlockType.FOR, thisNodePointer) {
                        action()
                    }

                    WriteOpProps.MULTIPLE_TOKEN -> {
                        if (phase != EncodePhase.SLOT) ctx.writeTokens(node, tokens, phase) { t -> t.multiple }
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
    phase: EncodePhase,
    filterPredicate: (EnrichedTreeAttr.Token) -> Boolean = { true }
) {
    val filteredTokens = tokens.filter(filterPredicate)
    val typeDecl = node.type.declaration
    if (typeDecl.isEnum() || typeDecl.isDataObject()) return

    val isBoolToken = typeDecl.isBool() && filteredTokens.isNotEmpty()
    if (isBoolToken) builder.beginControlFlow("if(${pointedParameter(node.name)})")

    filteredTokens.forEach {
        if (phase == EncodePhase.SIZE) {
            if (context.currentCommand.haveVaryingSize) appendLine("argCount += 1")
        } else if (phase == EncodePhase.WRITE) {
            addImport("eu.vendeli.rethis.utils.writeBulkString")
            context[CollectedTokens]?.addToken(it.name)
            val tokenProperty = tokenToRedisTokenPropertyName(it.name)
            addImport("eu.vendeli.rethis.utils.RedisToken")
            appendLine("buffer.writeBulkString(RedisToken.%L)", tokenProperty)
        }
        node.attr.remove(it)
    }

    if (isBoolToken) builder.endControlFlow()
}
