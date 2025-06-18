package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSType
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
        return handleSealedNode(node, sealedType)
    }

    // --- 2. Splice abstract grouping nodes ---
    if (node.rSpec == null) {
        val childOps = node.children
            .sortedBy { it.rangeBounds().first }
            .flatMap { recurse(it) }
        if (node.ks.type == SymbolType.Function || node.attr.none { it is EnrichedTreeAttr.Name }) return childOps
        return listOf(handleWrappedCall(node, node.type, childOps))
    }

    // --- 3. Leaf nodes: enum, data object, or primitive ---
    val typeAttr = node.attr.filterIsInstance<EnrichedTreeAttr.Type>().single()
    val kt = typeAttr.type

    if (kt.declaration.isEnum() || kt.declaration.isDataObject() || kt.declaration.isStdType()) {
        val name = nearestName(node)!!
        val directCall = WriteOp.DirectCall(
            isKey = node.attr.contains(EnrichedTreeAttr.Key),
            slotBlock = {
                appendLine(
                    "slot = validateSlot(slot, CRC16.lookup(${pointedParameter(name)}.toString().toByteArray(charset)))",
                )
            },
            encodeBlock = { inferWriting(kt, name, node, it) },
        )

        return wrapIfNeeded(node, directCall)
    }

    // --- 4. Complex types with children ---
    if (node.children.isNotEmpty()) {
        val childOps = node.children
            .sortedBy { it.rangeBounds().first }
            .flatMap { recurse(it) }
        return listOf(handleWrappedCall(node, kt, childOps))
    }

    // --- 5. Nothing to write ---
    return emptyList()
}

private fun nearestName(node: EnrichedNode?): String? = node?.attr
    ?.filterIsInstance<EnrichedTreeAttr.Name>()
    ?.singleOrNull()
    ?.name ?: nearestName(node?.parent)

private fun CodeGenContext.inferWriting(
    fieldType: KSType,
    fieldAccess: String,
    node: EnrichedNode,
    complex: Boolean,
) {
    val resolvedType = fieldType.collectionAwareType()
    val writeFn = "write${resolvedType.declaration.simpleName.asString()}Arg"
    val pName = if (complex) "$pointer.$fieldAccess" else pointedParameter(fieldAccess)
    val isBoolToken = resolvedType.declaration.isBool() && node.tokens.isNotEmpty()

    if (isBoolToken) builder.beginControlFlow("if($pName)")
    if (!resolvedType.declaration.isEnum() && !resolvedType.declaration.isDataObject()) {
        addImport("eu.vendeli.rethis.utils.writeStringArg")
        node.tokens.forEach {
            if (context.currentCommand.haveVaryingSize) appendLine("size += 1")
            appendLine("buffer.writeStringArg(\"${it.name}\", charset)")
        }
    }

    if (isBoolToken) {
        builder.endControlFlow()
        return
    }

    if (context.currentCommand.haveVaryingSize) builder.addStatement("size += 1")

    when {
        resolvedType.declaration.isStdType() -> {
            addImport("eu.vendeli.rethis.utils.$writeFn")
            val additionalParams = buildList {
                when {
                    resolvedType.declaration.isTimeType() -> {
                        addImport("eu.vendeli.rethis.api.spec.common.types.TimeUnit")
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
            appendLine("buffer.writeStringArg(%L.toString(), charset)", pointer ?: fieldAccess)
        }
    }
}

private fun handleSealedNode(node: EnrichedNode, sealedType: KSType): List<WriteOp> {
    // Flatten any intermediate wrappers
    val directSubs = node.children
        .sortedBy { it.rangeBounds().first }
        .flatMap { child ->
            if (child.rSpec == null && child.type.declaration.isSealed()) child.children else listOf(
                child,
            )
        }

    // Generate dispatch branches
    val branches = directSubs.associate { child -> child.type to recurse(child) }
    val dispatchOp = WriteOp.Dispatch(
        paramName = node.attr.filterIsInstance<EnrichedTreeAttr.Name>().singleOrNull()?.name,
        sealedType = sealedType,
        branches = branches,
    )

    // Wrap for vararg or nullable
    return wrapIfNeeded(node, dispatchOp)
}

private fun EnrichedNode.collectWriteOps(): Set<WriteOpProps> = listOfNotNull(
    WriteOpProps.NULLABLE.takeIf {
        attr
            .filterIsInstance<EnrichedTreeAttr.Optional>()
            .any { it.local == OptionalityType.Nullable }
    },
    WriteOpProps.WITH_SIZE.takeIf { attr.contains(EnrichedTreeAttr.SizeParam) },
    WriteOpProps.COLLECTION.takeIf {
        attr
            .filterIsInstance<EnrichedTreeAttr.Multiple>()
            .any { it.collection || it.vararg }
    },
).sortedBy { it.ordinal }.toSet()

private fun wrapIfNeeded(node: EnrichedNode, innerOp: WriteOp): List<WriteOp> =
    node.collectWriteOps()
        .takeIf { it.isNotEmpty() }
        ?.let {
            listOf(
                WriteOp.WrappedCall(
                    paramName = node.name,
                    elementType = node.attr.filterIsInstance<EnrichedTreeAttr.Type>().single().type,
                    props = it,
                    inner = listOf(innerOp),
                    tokens = emptyList(),
                    // wrapIfNeeded goes only for parameters and its tokens handled separately
                ),
            )
        } ?: listOf(innerOp)

private fun handleWrappedCall(
    node: EnrichedNode,
    kt: KSType,
    innerOps: List<WriteOp>,
): WriteOp.WrappedCall = WriteOp.WrappedCall(
    paramName = node.name,
    elementType = kt,
    props = node.collectWriteOps(),
    inner = innerOps,
    tokens = node.children.singleOrNull()?.tokens.orEmpty(),
)
