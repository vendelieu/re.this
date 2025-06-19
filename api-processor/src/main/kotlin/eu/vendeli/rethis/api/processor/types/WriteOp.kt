package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.context.CodeGenContext.BlockType
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

internal sealed class WriteOp {
    data class DirectCall(
        val isKey: Boolean,
        val slotBlock: CodeGenContext.(Boolean) -> Unit,
        val encodeBlock: CodeGenContext.(Boolean) -> Unit,
    ) : WriteOp()

    data class WrappedCall(
        val paramName: String,
        val elementType: KSType,
        val props: Set<WriteOpProps>,
        val inner: List<WriteOp>,
        val tokens: List<EnrichedTreeAttr.Token>,
    ) : WriteOp()

    data class Dispatch(
        val paramName: String?,
        val sealedType: KSType,
        val branches: Map<KSType, List<WriteOp>>,
    ) : WriteOp()
}

internal enum class WriteOpProps {
    NULLABLE, WITH_SIZE, COLLECTION
}

internal fun WriteOp.filterWithoutKey(): WriteOp? = when (this) {
    is WriteOp.DirectCall -> if (isKey) this else null
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
        is WriteOp.DirectCall -> if (!encode && isKey) slotBlock(ctx, complex) else encodeBlock(ctx, complex)

        is WriteOp.Dispatch -> ctx.buildBlock(paramName ?: ctx.pointer!!, BlockType.WHEN) {
            branches.forEach { (subType, branchOps) ->
                addImport(subType.declaration.qualifiedName!!.asString())
                ctx.builder.beginControlFlow("is ${subType.name} -> ")
                if (encode) subType.declaration.getAnnotationsByType(RedisOption.Token::class).forEach {
                    if(context.currentCommand.haveVaryingSize) ctx.appendLine("size += 1")
                    ctx.appendLine("buffer.writeStringArg(\"${it.name}\", charset)")
                }
                branchOps.forEach { it.emitOp(encode) }
                ctx.builder.endControlFlow()
            }
            if (!encode) ctx.builder.addStatement("else -> {}")
        }

        is WriteOp.WrappedCall -> {
            val isComplex = !elementType.declaration.isStdType() &&
                elementType.declaration.safeCast<KSClassDeclaration>()?.primaryConstructor?.parameters?.size?.let {
                    it > 0
                } == true

            fun handleWrapping(type: ArrayDeque<WriteOpProps>) {
                val action = {
                    when {
                        type.isNotEmpty() -> handleWrapping(type)
                        else -> {
                            if (tokens.isNotEmpty()) addImport("eu.vendeli.rethis.utils.writeStringArg")
                            tokens.forEach {
                                if (context.currentCommand.haveVaryingSize) ctx.appendLine("size += 1")
                                ctx.appendLine("buffer.writeStringArg(\"${it.name}\", charset)")
                            }
                            inner.forEach { it.emitOp(encode, isComplex) }
                        }
                    }
                }
                when (type.removeFirstOrNull() ?: return) {
                    WriteOpProps.NULLABLE -> {
                        ctx.buildBlock(paramName, BlockType.LET) { action() }
                    }

                    WriteOpProps.WITH_SIZE -> if (encode) {
                        addImport("eu.vendeli.rethis.utils.writeIntArg")
                        ctx.appendLine("buffer.writeIntArg(%L.size, charset)", ctx.pointedParameter(paramName))
                        action()
                    }

                    WriteOpProps.COLLECTION -> {
                        ctx.buildBlock(paramName, BlockType.FOR) { action() }
                    }
                }
            }
            handleWrapping(ArrayDeque(props))
        }
    }
}
