package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.context.CollectedTokens
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.shared.annotations.RedisOption

internal fun buildWritePlan(root: EnrichedNode): List<WriteOp> = recurse(root)

private fun recurse(node: EnrichedNode): List<WriteOp> {
    // --- 1. Sealed-class dispatch (possibly wrapped for vararg/nullable) ---
    val sealedType = node.attr
        .filterIsInstance<EnrichedTreeAttr.Type>()
        .map { it.type }
        .firstOrNull()
        ?.takeIf { it.declaration.isSealed() }

    if (sealedType != null) {
        return handleSealedNode(node)
    }

    // --- 2. Splice abstract grouping nodes ---
    if (node.rSpec == null) {
        val childOps = node.children
            .sortedWith(compareBy({ it.rangeBounds().first }, { it.rangeBounds().second }))
            .flatMap { recurse(it) }
        if (node.ks.type == SymbolType.Function || node.attr.none { it is EnrichedTreeAttr.Name }) return childOps
        return listOf(handleWrappedCall(node, childOps))
    }

    // --- 3. Leaf nodes: enum, data object, or primitive ---
    val typeAttr = node.attr.filterIsInstance<EnrichedTreeAttr.Type>().single()
    val kt = typeAttr.type

    if (kt.declaration.isEnum() || kt.declaration.isDataObject() || kt.declaration.isStdType()) {
        val name = nearestName(node)!!
        val directCall = WriteOp.DirectCall(
            node = node,
            slotBlock = { parentPointer ->
                val toString = if (
                    kt.name != "String" &&
                    kt.arguments.firstOrNull { t -> t.type?.resolve()?.name == "String" } == null
                ) ".toString()" else ""
                val pName = when {
                    blockStack.isNotEmpty() -> {
                        val iteratorVar = pointer ?: name
                        val isChildOfBlockField = parentPointer != null && parentPointer == blockStack.last().second
                        if ((blockStack.last().second != name || isChildOfBlockField) && iteratorVar != name) {
                            "$iteratorVar.$name"
                        } else {
                            iteratorVar
                        }
                    }

                    !parentPointer.isNullOrBlank() -> "$parentPointer.$name"
                    else -> name
                }
                appendLine(
                    "slot = validateSlot(slot, CRC16.lookup(%L$toString.toByteArray(charset)))",
                    pName,
                )
            },
            encodeBlock = { parentPointer, phase -> inferWriting(name, node, parentPointer, phase) },
        )
        return wrapIfNeeded(node, directCall)
    }

    // --- 4. Complex types with children ---
    if (node.children.isNotEmpty()) {
        val childOps = node.children
            .sortedWith(compareBy({ it.rangeBounds().first }, { it.rangeBounds().second }))
            .flatMap { recurse(it) }
        return listOf(handleWrappedCall(node, childOps))
    }

    // --- 5. Nothing to write ---
    return emptyList()
}

private fun nearestName(node: EnrichedNode?): String? = node?.attr
    ?.filterIsInstance<EnrichedTreeAttr.Name>()
    ?.singleOrNull()
    ?.name ?: nearestName(node?.parent)

@OptIn(KspExperimental::class)
private fun CodeGenContext.inferWriting(
    fieldAccess: String,
    node: EnrichedNode,
    parentPointer: String?,
    phase: EncodePhase,
) {
    val resolvedType = node.type.collectionAwareType()

    writeTokens(node, node.tokens, phase)
    if (resolvedType.declaration.isBool()) return

    // Build the parameter name based on context
    val pName = when {
        // Inside a block (forEach, let, when) - use iterator.fieldName or just iterator if same name
        blockStack.isNotEmpty() -> {
            val iteratorVar = pointer ?: fieldAccess
            val isChildOfBlockField = parentPointer != null && parentPointer == blockStack.last().second
            if ((blockStack.last().second != fieldAccess || isChildOfBlockField) && iteratorVar != fieldAccess) {
                "$iteratorVar.$fieldAccess"
            } else {
                iteratorVar
            }
        }
        // Has explicit parent pointer (e.g., streams.key)
        !parentPointer.isNullOrBlank() -> "$parentPointer.$fieldAccess"
        // Top level - just use the parameter name
        else -> fieldAccess
    }

    if (phase == EncodePhase.SIZE) {
        if (context.currentCommand.haveVaryingSize) builder.addStatement("argCount += 1")
    } else if (phase == EncodePhase.WRITE) {
        addImport("eu.vendeli.rethis.utils.writeBulkString")
        addImport("io.ktor.utils.io.core.toByteArray")
        when {
            resolvedType.declaration.isStdType() -> {
                when {
                    resolvedType.declaration.isString() -> {
                        appendLine("buffer.writeBulkString(%L.toByteArray(charset))", pName)
                    }

                    resolvedType.declaration.isInstant() -> {
                        val timeUnit = resolvedType.getTimeUnit()
                        val expr = if (timeUnit == "MILLISECONDS") "toEpochMilliseconds()" else "epochSeconds"
                        appendLine("buffer.writeBulkString(%L.%L.toString().toByteArray(charset))", pName, expr)
                    }

                    resolvedType.declaration.isDuration() -> {
                        val timeUnit = resolvedType.getTimeUnit()
                        val expr = if (timeUnit == "MILLISECONDS") "inWholeMilliseconds" else "inWholeSeconds"
                        appendLine("buffer.writeBulkString(%L.%L.toString().toByteArray(charset))", pName, expr)
                    }

                    resolvedType.declaration.isByteArray() -> {
                        appendLine("buffer.writeBulkString(%L)", pName)
                    }

                    resolvedType.declaration.isCharArray() -> {
                        appendLine("buffer.writeBulkString(%L.concatToString().toByteArray(charset))", pName)
                    }

                    else -> appendLine("buffer.writeBulkString(%L.toString().toByteArray(charset))", pName)
                }
            }

            resolvedType.declaration.isDataObject() -> {
                // For data objects, get token from the declaration itself
                @OptIn(KspExperimental::class)
                val declTokens = (resolvedType.declaration as? KSClassDeclaration)
                    ?.getAnnotationsByType(RedisOption.Token::class)?.map { it.name }?.toList()
                val actualTokens = if (!declTokens.isNullOrEmpty()) declTokens else node.tokens.map { it.name }
                val declarationToken = resolvedType.declaration.effectiveName()

                if (actualTokens.singleOrNull() == declarationToken) {
                    appendLine(
                        "buffer.writeBulkString(%L.toString().toByteArray(charset))",
                        pointer ?: fieldAccess,
                    )
                } else {
                    actualTokens.forEach { tokenName ->
                        // Collect token and use RedisToken
                        context[CollectedTokens]?.addToken(tokenName)
                        val tokenProperty = tokenToRedisTokenPropertyName(tokenName)
                        addImport("eu.vendeli.rethis.utils.RedisToken")
                        addImport("eu.vendeli.rethis.utils.writeBulkString")
                        appendLine("buffer.writeBulkString(RedisToken.%L)", tokenProperty)
                    }
                }
            }

            resolvedType.declaration.isEnum() -> {
                appendLine("buffer.writeBulkString(%L.toString().toByteArray(charset))", pName)
            }
        }
    }
}

private fun handleSealedNode(node: EnrichedNode): List<WriteOp> {
    // Flatten any intermediate wrappers
    val directSubs = node.children
        .sortedWith(compareBy({ it.rangeBounds().first }, { it.rangeBounds().second }))
        .flatMap { child ->
            if (child.rSpec == null && child.type.declaration.isSealed()) child.children else listOf(
                child,
            )
        }

    // Generate dispatch branches
    val branches = directSubs.associate { child -> child.type to recurse(child) }
    val dispatchOp = WriteOp.Dispatch(
        node = node,
        branches = branches,
    )

    // Wrap for vararg or nullable
    return wrapIfNeeded(node, dispatchOp)
}

private fun EnrichedNode.collectWriteOps(
    extraTokens: List<EnrichedTreeAttr.Token>? = null,
): Set<WriteOpProps> = listOfNotNull(
    WriteOpProps.NULLABLE.takeIf {
        attr
            .filterIsInstance<EnrichedTreeAttr.Optional>()
            .any { it.local == OptionalityType.Nullable }
    },
    WriteOpProps.WITH_SIZE.takeIf { attr.contains(EnrichedTreeAttr.SizeParam) },
    WriteOpProps.SINGLE_TOKEN.takeIf { (extraTokens ?: tokens).any { !it.multiple } },
    WriteOpProps.COLLECTION.takeIf {
        attr
            .filterIsInstance<EnrichedTreeAttr.Multiple>()
            .any { it.collection || it.vararg }
    },
    WriteOpProps.MULTIPLE_TOKEN.takeIf { (extraTokens ?: tokens).any { it.multiple } },
).sortedBy { it.ordinal }.toSet()

private fun wrapIfNeeded(node: EnrichedNode, innerOp: WriteOp): List<WriteOp> =
    node.collectWriteOps()
        .takeIf { it.isNotEmpty() }
        ?.let {
            listOf(
                WriteOp.WrappedCall(
                    node = node,
                    props = it.toMutableSet(),
                    inner = listOf(innerOp),
                    tokens = node.tokens.toMutableList(),
                ),
            )
        } ?: listOf(innerOp)

private fun handleWrappedCall(
    node: EnrichedNode,
    innerOps: List<WriteOp>,
): WriteOp.WrappedCall = node.children.singleOrNull()?.tokens.orEmpty().let {
    WriteOp.WrappedCall(
        node = node,
        props = node.collectWriteOps(it).toMutableSet(),
        inner = innerOps,
        tokens = it.toMutableList(),
    )
}
