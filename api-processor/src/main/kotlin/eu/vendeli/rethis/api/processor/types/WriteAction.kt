package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

internal sealed class WriteAction {
    abstract val paramName: String
    abstract val nullable: Boolean
    abstract val slotCountExpr: CodeBlock
    abstract val rSpecPath: List<Int>
    open val isKey: Boolean = false
    abstract fun emitBody(code: CodeBlock.Builder)

    /** emits slots into slotSet */
    open fun emitSlotLookup(code: CodeBlock.Builder) {}

    fun emit(code: CodeBlock.Builder) {
        if (nullable) {
            code.beginControlFlow("if (%L != null)", paramName)
            emitBody(code)
            code.endControlFlow()
        } else {
            emitBody(code)
        }
    }

    fun emitSlot(code: CodeBlock.Builder) {
        if (!isKey) return
        if (nullable) code.beginControlFlow("if (%L != null)", paramName)
        emitSlotLookup(code)
        if (nullable) code.endControlFlow()
    }
}

internal data class SimpleAction(
    override val paramName: String,
    private val writeCall: CodeBlock,
    override val nullable: Boolean,
    override val rSpecPath: List<Int>,
    override val isKey: Boolean = false,
) : WriteAction() {
    override val slotCountExpr = if (nullable)
        CodeBlock.of("(if (%L != null) 1 else 0)", paramName)
    else CodeBlock.of("1")

    override fun emitBody(code: CodeBlock.Builder) {
        code.addStatement("%L", writeCall)
    }

    override fun emitSlotLookup(code: CodeBlock.Builder) {
        code.addStatement("slotSet += CRC16.lookup(%L.toByteArray(charset))", paramName)
    }
}

internal data class CollectionAction(
    override val paramName: String,
    override val nullable: Boolean,
    override val rSpecPath: List<Int>,
    private val elementBuilder: (String) -> WriteAction,
) : WriteAction() {
    override val slotCountExpr = if (nullable)
        CodeBlock.of("(%L?.size ?: 0)", paramName)
    else CodeBlock.of("%L.size", paramName)

    override val isKey = true

    override fun emitBody(code: CodeBlock.Builder) {
        code.beginControlFlow("%L.forEach", paramName)
        elementBuilder("it").emitBody(code)
        code.endControlFlow()
    }


    override fun emitSlotLookup(code: CodeBlock.Builder) {
        // recurse down
        code.beginControlFlow("%L.forEach { elt ->", paramName)
        elementBuilder("elt").emitSlotLookup(code)
        code.endControlFlow()
    }
}

internal data class CompositeAction(
    override val paramName: String,
    override val nullable: Boolean,
    private val children: List<WriteAction>,
    override val rSpecPath: List<Int>,
) : WriteAction() {
    override val slotCountExpr = if (nullable)
        CodeBlock.of("(if (%L != null) 1 else 0)", paramName)
    else CodeBlock.of("1")

    override val isKey = true

    override fun emitBody(code: CodeBlock.Builder) {
        children.forEach { it.emit(code) }
    }

    override fun emitSlotLookup(code: CodeBlock.Builder) {
        children.forEach { it.emitSlot(code) }
    }
}

// a bit more complex because it defers helper creation
internal data class SealedAction(
    override val paramName: String,
    override val nullable: Boolean,
    private val containerType: KSType,
    private val isVararg: Boolean,
    override val rSpecPath: List<Int>,
    private val helpersNeeded: MutableMap<String, KSClassDeclaration>,
) : WriteAction() {
    override val slotCountExpr = if (nullable)
        CodeBlock.of("(if ($paramName != null) ${if (isVararg) "$paramName.size" else "1"} else 0)")
    else CodeBlock.of(if (isVararg) "$paramName.size" else "1")
    override val isKey = true

    override fun emitBody(code: CodeBlock.Builder) {
        code.beginControlFlow("when (%L)", paramName)
        containerType.declaration.safeCast<KSClassDeclaration>()!!.getSealedSubclasses().forEach { sub ->
            code.beginControlFlow("is %T ->", sub.toClassName())
            when {
                sub.isDataObject() || sub.isEnum() -> {
                    addImport("eu.vendeli.rethis.utils.writeStringArg")
                    code.addStatement("buffer.writeStringArg(%L.toString(), charset)", paramName)
                }

                sub.hasAnnotation<RedisOptionContainer>() -> {
                    code.beginControlFlow("when (%L)", paramName)
                    sub.getSealedSubclasses().forEach {
                        code.beginControlFlow("is %T ->", it.toClassName())
                        val fn = registerVariantHelper(it, helpersNeeded)
                        code.addStatement("buffer.%L(%L, charset)", fn, paramName)
                        code.endControlFlow()
                    }
                    code.endControlFlow()
                }

                else -> {
                    val fn = registerVariantHelper(sub, helpersNeeded)
                    code.addStatement("buffer.%L(%L, charset)", fn, paramName)
                }
            }
            code.endControlFlow()
        }
        code.endControlFlow()
    }

    override fun emitSlotLookup(code: CodeBlock.Builder) {
        // very similar to emitBody but do `.emitSlotLookup` for each variant
        code.beginControlFlow("when (%L)", paramName)
        containerType.declaration.safeCast<KSClassDeclaration>()!!
            .getSealedSubclasses().map { sub ->
                sub.toClassName() to sub.primaryConstructor?.parameters?.mapNotNull { v ->
                    val isKey = context.libSpecTree.findParameterByName(v.name!!.asString())?.let {
                        findPairNode(it)?.safeCast<RSpecNode.Simple>()?.keyIdx != null
                    } ?: false
                    if (!isKey) return@mapNotNull null

                    v
                }.orEmpty()
            }.filter { it.second.isNotEmpty() }.forEach {
                code.beginControlFlow("is %T ->", it.first)
                it.second.forEach { p ->
                    val pName = p.name!!.asString()
                    if (p.isVararg || p.type.resolve().isCollection()) {
                        code.beginControlFlow(
                            "%L.%L.forEach { k ->",
                            if (isVararg || containerType.isCollection()) "elt" else pName,
                            pName,
                        )
                        code.addStatement("slotSet += CRC16.lookup(k.toByteArray(charset))")
                        code.endControlFlow()
                        return@forEach
                    }
                    code.addStatement(
                        "slotSet += CRC16.lookup(%L.toByteArray(charset))",
                        if (isVararg || containerType.isCollection()) "elt.$pName" else "$paramName.$pName",
                    )
                }
                code.endControlFlow()
            }
        code.beginControlFlow("else ->")
        code.endControlFlow()

        code.endControlFlow()
    }
}
