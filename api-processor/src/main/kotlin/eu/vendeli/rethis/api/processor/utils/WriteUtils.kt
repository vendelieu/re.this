package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.*

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
            slotBlock = {
                val toString = if (
                    kt.name != "String" &&
                    kt.arguments.firstOrNull { t -> t.type?.resolve()?.name == "String" } == null
                ) ".toString()" else ""
                appendLine(
                    "slot = validateSlot(slot, CRC16.lookup(${
                        pointedParameter(
                            name,
                            isComplex = it,
                        )
                    }$toString.toByteArray(charset)))",
                )
            },
            encodeBlock = { inferWriting(name, node, it) },
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
    complex: Boolean,
) {
    val resolvedType = node.type.collectionAwareType()

    writeTokens(node, node.tokens)
    if (resolvedType.declaration.isBool()) return

    val writeFn = "write${resolvedType.declaration.simpleName.asString()}Arg"
    val pName = if (complex) "$pointer.$fieldAccess" else pointedParameter(fieldAccess)

    if (context.currentCommand.haveVaryingSize) builder.addStatement("size += 1")

    when {
        resolvedType.declaration.isStdType() -> {
            addImport("eu.vendeli.rethis.utils.$writeFn")
            val additionalParams = buildList {
                when {
                    resolvedType.declaration.isTimeType() -> {
                        addImport("eu.vendeli.rethis.shared.types.TimeUnit")
                        add("TimeUnit.${resolvedType.getTimeUnit()}")
                    }
                }
            }.joinToString(prefix = ", ")
            appendLine("buffer.%L(%L, charset$additionalParams)", writeFn, pName)
        }

        resolvedType.declaration.isDataObject() -> {
            addImport("eu.vendeli.rethis.utils.writeStringArg")
            val actualTokens = node.tokens.map { it.name }
            val declarationToken = resolvedType.declaration.effectiveName()

            if (actualTokens.singleOrNull() == declarationToken) {
                appendLine("buffer.writeStringArg(%L.toString(), charset)", pointer ?: fieldAccess)
            } else actualTokens.forEach {
                appendLine("buffer.writeStringArg(\"$it\", charset)")
            }
        }

        resolvedType.declaration.isEnum() -> {
            addImport("eu.vendeli.rethis.utils.writeStringArg")
            appendLine("buffer.writeStringArg(%L.toString(), charset)", pointedParameter(fieldAccess))
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
