package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import eu.vendeli.rethis.api.processor.context.*
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
        // Fallback for spec-less primitive params (e.g. `@RIgnoreSpecAbsence vararg X: String`):
        // emit a per-element DirectCall so the variadic args actually reach the wire.
        // Data-object sealed subtypes have no Name attr but still need the DirectCall path so
        // their `@RedisOption.Token` is emitted when the upstream spec doesn't carry that token.
        if (childOps.isEmpty() && node.children.isEmpty() && node.ks.type != SymbolType.Function) {
            val typeAttr = node.attr.filterIsInstance<EnrichedTreeAttr.Type>().singleOrNull()
            val hasName = node.attr.any { it is EnrichedTreeAttr.Name }
            val isSpeclessDataObject = typeAttr?.type?.declaration?.isDataObject() == true
            if (typeAttr != null && (hasName || isSpeclessDataObject)) {
                val kt = typeAttr.type
                if (kt.declaration.isEnum() || kt.declaration.isDataObject() || kt.declaration.isStdType()) {
                    val name = nearestName(node)!!
                    val directCall = WriteOp.DirectCall(
                        node = node,
                        slotBlock = { parent ->
                            val toString = if (
                                kt.name != "String" &&
                                kt.arguments.firstOrNull { t -> t.type?.resolve()?.name == "String" } == null
                            ) ".toString()" else ""
                            appendLine(
                                "slot = validateSlot(slot, CRC16.lookup(%L$toString.toByteArray(charset)))",
                                parent.qualify(name).render(),
                            )
                        },
                        encodeBlock = { parent, phase -> inferWriting(name, node, parent, phase) },
                    )
                    return wrapIfNeeded(node, directCall)
                }
            }
        }
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
            slotBlock = { parent ->
                val toString = if (
                    kt.name != "String" &&
                    kt.arguments.firstOrNull { t -> t.type?.resolve()?.name == "String" } == null
                ) ".toString()" else ""
                appendLine(
                    "slot = validateSlot(slot, CRC16.lookup(%L$toString.toByteArray(charset)))",
                    parent.qualify(name).render(),
                )
            },
            encodeBlock = { parent, phase -> inferWriting(name, node, parent, phase) },
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
    field: String,
    node: EnrichedNode,
    parent: Access,
    phase: EncodePhase,
) {
    val resolvedType = node.type.collectionAwareType()

    writeTokens(node, node.tokens, parent, phase)
    if (resolvedType.declaration.isBool()) return

    if (phase == EncodePhase.SIZE) {
        // Counting bodies don't need the access path — skip rendering it so the binding stays unmarked.
        if (context.currentCommand.haveVaryingSize) {
            val argInc = if (resolvedType.declaration.isDataObject()) {
                val declTokens = resolvedType.declaration
                    .safeCast<KSClassDeclaration>()
                    ?.getAnnotationsByType(RedisOption.Token::class)
                    ?.map { it.name }
                    ?.toList()
                val actualTokens = if (!declTokens.isNullOrEmpty()) declTokens else node.tokens.map { it.name }
                val declarationToken = resolvedType.declaration.effectiveName()
                when {
                    actualTokens.isEmpty() -> 1
                    actualTokens.singleOrNull() == declarationToken -> 1
                    else -> actualTokens.size
                }
            } else 1
            appendLine("argCount += $argInc")
        }
        return
    }
    if (phase != EncodePhase.WRITE) return

    val pName = parent.qualify(field).render()
    addImport("eu.vendeli.rethis.utils.writeBulkString")
    addImport("io.ktor.utils.io.core.toByteArray")
    when {
        resolvedType.declaration.isStdType() -> {
            when {
                resolvedType.declaration.isString() -> {
                    appendLine("buffer.writeBulkString(%L.toByteArray(charset))", pName)
                }

                resolvedType.declaration.isInt() -> {
                    addImport("eu.vendeli.rethis.utils.writeIntArg")
                    appendLine("buffer.writeIntArg(%L, charset)", pName)
                }

                resolvedType.declaration.isLong() -> {
                    addImport("eu.vendeli.rethis.utils.writeLongArg")
                    appendLine("buffer.writeLongArg(%L, charset)", pName)
                }

                resolvedType.declaration.isInstant() -> {
                    addImport("eu.vendeli.rethis.utils.writeLongArg")
                    val timeUnit = resolvedType.getTimeUnit()
                    val expr = if (timeUnit == "MILLISECONDS") "toEpochMilliseconds()" else "epochSeconds"
                    appendLine("buffer.writeLongArg(%L.%L, charset)", pName, expr)
                }

                resolvedType.declaration.isDuration() -> {
                    addImport("eu.vendeli.rethis.utils.writeLongArg")
                    val timeUnit = resolvedType.getTimeUnit()
                    val expr = if (timeUnit == "MILLISECONDS") "inWholeMilliseconds" else "inWholeSeconds"
                    appendLine("buffer.writeLongArg(%L.%L, charset)", pName, expr)
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
            val declTokens = resolvedType.declaration
                .safeCast<KSClassDeclaration>()
                ?.getAnnotationsByType(RedisOption.Token::class)
                ?.map { it.name }
                ?.toList()
            val actualTokens = if (!declTokens.isNullOrEmpty()) declTokens else node.tokens.map { it.name }

            actualTokens.forEach { tokenName ->
                // Use the pre-baked RedisToken constant — even when the token name happens to
                // match the declaration's class name (e.g. Q8), avoid runtime `toString()`
                // allocation in favour of the cached ByteArray.
                context[CollectedTokens]?.addToken(tokenName)
                val tokenProperty = tokenToRedisTokenPropertyName(tokenName)
                addImport("eu.vendeli.rethis.utils.RedisToken")
                addImport("eu.vendeli.rethis.utils.writeBulkString")
                appendLine("buffer.writeBulkString(RedisToken.%L)", tokenProperty)
            }
        }

        resolvedType.declaration.isEnum() -> {
            appendLine("buffer.writeBulkString(%L.toString().toByteArray(charset))", pName)
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
): WriteOp.WrappedCall {
    // Prefer the single child's own tokens when present (LCS/MinMatchLen-style classes carry
    // their own `@RedisOption.Token`, which "bubble up" to be emitted at the parent wrap).
    // Otherwise fall back to the param's own tokens — but ONLY at the value-param level so
    // recursive inner class wraps don't re-emit the same token already handled by their
    // parent. This covers `vararg data: HFieldValue`-style params where the outer-block
    // token (HSETEX's `[FIELDS]`) is annotated on the param itself and the value class has
    // no tokens of its own.
    val childTokens = node.children.singleOrNull()?.tokens.orEmpty()
    val isParamLevel = node.attr.filterIsInstance<EnrichedTreeAttr.Symbol>()
        .any { it.type == SymbolType.ValueParam }
    val effectiveTokens = when {
        childTokens.isNotEmpty() -> childTokens
        isParamLevel -> node.tokens
        else -> emptyList()
    }
    return WriteOp.WrappedCall(
        node = node,
        props = node.collectWriteOps(effectiveTokens).toMutableSet(),
        inner = innerOps,
        tokens = effectiveTokens.toMutableList(),
    )
}
