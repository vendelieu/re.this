package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.context.Access
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.context.CollectedTokens
import eu.vendeli.rethis.api.processor.context.qualify
import eu.vendeli.rethis.api.processor.context.render
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
        val slotBlock: CodeGenContext.(Access) -> Unit,
        val encodeBlock: CodeGenContext.(Access, EncodePhase) -> Unit,
    ) : WriteOp()

    data class WrappedCall(
        override val node: EnrichedNode,
        val props: Set<WriteOpProps>,
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

internal fun WriteOp.emitOp(phase: EncodePhase, parent: Access = Access.Top) {
    val ctx = context[CodeGenContext] ?: run {
        context.logger.error("CodeGenContext not found")
        return
    }
    when (this) {
        is WriteOp.DirectCall -> emit(ctx, phase, parent)
        is WriteOp.Dispatch -> emit(ctx, phase, parent)
        is WriteOp.WrappedCall -> emit(ctx, phase, parent)
    }
}

// region DirectCall

private fun WriteOp.DirectCall.emit(ctx: CodeGenContext, phase: EncodePhase, parent: Access) {
    when (phase) {
        EncodePhase.SLOT -> if (node.attr.contains(EnrichedTreeAttr.Key)) slotBlock(ctx, parent)
        EncodePhase.SIZE, EncodePhase.WRITE -> encodeBlock(ctx, parent, phase)
    }
}

// endregion

// region Dispatch

@OptIn(KspExperimental::class)
private fun WriteOp.Dispatch.emit(ctx: CodeGenContext, phase: EncodePhase, parent: Access) {
    // Top-level dispatch like `when (subcommand)` uses the node's own name; nested dispatch over a
    // binding (e.g. `when (it0)` inside `forEach` over a sealed element type) inherits `parent`.
    val subject = node.nameOrNull?.let { parent.qualify(it) } ?: parent
    require(subject != Access.Top) { "Dispatch requires a named subject" }
    ctx.inWhen(subject) {
        branches.forEach { (subType, branchOps) ->
            addImport(subType.declaration.qualifiedName!!.asString())
            ctx.inWhenBranch("is ${subType.name}") {
                val processedTokens = ctx.emitDispatchBranchPivot(phase, subType)
                // Inside `is X -> { … }` Kotlin smart-casts the subject to X — same access path.
                branchOps.forEach {
                    it.handleTokens(processedTokens)
                    it.emitOp(phase, subject)
                }
            }
        }
        if (phase == EncodePhase.SLOT) ctx.appendElseEmpty()
    }
}

@OptIn(KspExperimental::class)
private fun CodeGenContext.emitDispatchBranchPivot(
    phase: EncodePhase,
    subType: KSType,
): Set<String> {
    if (phase == EncodePhase.SLOT) return emptySet()
    val processed = mutableSetOf<String>()
    val isDataObject = subType.declaration.isDataObject()
    subType.declaration.getAnnotationsByType(RedisOption.Token::class).forEach { tok ->
        if (phase == EncodePhase.WRITE) context[CollectedTokens]?.addToken(tok.name)
        // For data objects the bound value itself stringifies to the token, so the pivot
        // contributes no extra arg/byte — only mark the token name as processed.
        if (!isDataObject) {
            when (phase) {
                EncodePhase.SIZE ->
                    if (context.currentCommand.haveVaryingSize) appendLine("argCount += 1")

                EncodePhase.WRITE -> {
                    addImport("eu.vendeli.rethis.utils.writeBulkString")
                    addImport("eu.vendeli.rethis.utils.RedisToken")
                    appendLine(
                        "buffer.writeBulkString(RedisToken.%L)",
                        tokenToRedisTokenPropertyName(tok.name),
                    )
                }
            }
        }
        processed.add(tok.name)
    }
    return processed
}

// endregion

// region WrappedCall

private fun WriteOp.WrappedCall.emit(ctx: CodeGenContext, phase: EncodePhase, parent: Access) {
    val shape = WrapShape.of(props)
    val isComplex = node.isComplexType()
    val nodeName = node.nameOrNull
    // `node.name` (single Name attr) is only guaranteed when one of these props demands it.
    // Path-4 complex-with-children WrappedCalls can lack a Name, so guard the access.
    val needsSelfAccess = shape.nullable || shape.collection || shape.withSize
    val selfAccess: Access? = if (needsSelfAccess) parent.qualify(node.name) else null
    val childParent: Access =
        if (isComplex && !shape.collection && nodeName != null) parent.qualify(nodeName) else parent

    fun emitTokensAndSize(current: Access) {
        if (phase == EncodePhase.SLOT) return
        if (shape.singleToken) ctx.writeTokens(node, tokens, current, phase) { !it.multiple }
        if (shape.withSize) ctx.emitSizePrefix(selfAccess!!, phase)
    }

    fun emitGuarded(current: Access) {
        if (shape.needsGuard && phase != EncodePhase.SLOT) {
            ctx.inScope("if (%L.isNotEmpty())", selfAccess!!.render()) { emitTokensAndSize(current) }
        } else {
            emitTokensAndSize(current)
        }
    }

    fun emitTail(current: Access) {
        if (shape.multipleToken && phase != EncodePhase.SLOT) {
            ctx.writeTokens(node, tokens, current, phase) { it.multiple }
        }
        inner.forEach { it.emitOp(phase, current) }
    }

    fun emitBody(current: Access, forTarget: Access) {
        emitGuarded(current)
        if (shape.collection) ctx.inFor(forTarget) { binding -> emitTail(binding.ref()) }
        else emitTail(current)
    }

    if (shape.nullable) {
        ctx.inLet(selfAccess!!) { binding ->
            // For `Foo?` of a complex (data class) wrapper, children read `foo.field` via Kotlin
            // smart-cast inside the let — the LET binding stays unused → param `_`. For everything
            // else, children read `binding.field`.
            val current = if (isComplex && !shape.collection) childParent else binding.ref()
            emitBody(current, binding.ref())
        }
    } else {
        emitBody(childParent, selfAccess ?: childParent)
    }
}

private class WrapShape(
    val nullable: Boolean,
    val singleToken: Boolean,
    val withSize: Boolean,
    val collection: Boolean,
    val multipleToken: Boolean,
) {
    // Wraps SINGLE_TOKEN's tokens + WITH_SIZE's size emission; the subsequent forEach is OUTSIDE
    // the guard so an empty collection does not emit a stray "TOKEN 0" arg.
    val needsGuard: Boolean get() = singleToken && collection

    companion object {
        fun of(props: Set<WriteOpProps>) = WrapShape(
            nullable = WriteOpProps.NULLABLE in props,
            singleToken = WriteOpProps.SINGLE_TOKEN in props,
            withSize = WriteOpProps.WITH_SIZE in props,
            collection = WriteOpProps.COLLECTION in props,
            multipleToken = WriteOpProps.MULTIPLE_TOKEN in props,
        )
    }
}

private fun EnrichedNode.isComplexType(): Boolean {
    val decl = type.declaration
    if (decl.isStdType() || decl.isEnum()) return false
    val ctorParams = decl.safeCast<KSClassDeclaration>()?.primaryConstructor?.parameters?.size
    return ctorParams != null && ctorParams > 0
}

private fun CodeGenContext.emitSizePrefix(access: Access, phase: EncodePhase) {
    when (phase) {
        EncodePhase.SIZE -> appendLine("argCount += 1")
        EncodePhase.WRITE -> {
            addImport("eu.vendeli.rethis.utils.writeBulkString")
            appendLine(
                "buffer.writeBulkString(%L.size.toString().toByteArray(charset))",
                access.render(),
            )
        }

        EncodePhase.SLOT -> Unit
    }
}

// endregion

private fun WriteOp.handleTokens(processedTokens: Set<String>) {
    node.attr.removeIf { attr -> attr is EnrichedTreeAttr.Token && processedTokens.contains(attr.name) }
    if (this is WriteOp.WrappedCall) {
        tokens.removeIf { attr -> processedTokens.contains(attr.name) }
    }
}

internal fun CodeGenContext.writeTokens(
    node: EnrichedNode,
    tokens: List<EnrichedTreeAttr.Token>,
    parent: Access,
    phase: EncodePhase,
    filterPredicate: (EnrichedTreeAttr.Token) -> Boolean = { true },
) {
    val filteredTokens = tokens.filter(filterPredicate)
    val typeDecl = node.type.declaration
    if (typeDecl.isEnum() || typeDecl.isDataObject()) return

    val isBoolToken = typeDecl.isBool() && filteredTokens.isNotEmpty()
    if (isBoolToken) beginControlFlow("if(${parent.qualify(node.name).render()})")

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

    if (isBoolToken) endControlFlow()
}
