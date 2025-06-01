package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer
import kotlinx.io.Buffer

// -------------------------
// Encoder code generator
// -------------------------
private data class EncoderCtx(
    val fileSpec: FileSpec.Builder,
    val typeSpec: TypeSpec.Builder,
    val addedExt: MutableSet<String>,
)

internal fun FileSpec.Builder.buildEncoderCode(
    typeSpec: TypeSpec.Builder,
    annotation: Map<String, String>,
    parameters: List<KSValueParameter>,
    keyParam: KSValueParameter?,
): CodeBlock = CodeBlock.builder().apply {
    val ctx = EncoderCtx(this@buildEncoderCode, typeSpec, mutableSetOf())

    // 1) Build WriteAction for each parameter
    val actions = parameters.map { param ->
        buildActionForParam(param, ctx)
    }

    // 2) Compute size expression
    val sizeExpr = actions.map { it.slotCountExpr }.join(" + ")

    addStatement("val buffer = Buffer()")
    if (actions.any { it.nullable }) {
        addStatement("val size = 1 + %L", sizeExpr)
        addStatement("buffer.writeString(\"\$size\")")
    }
    addStatement("COMMAND_HEADER.copyTo(buffer)")

    // 3) Emit all writes
    actions.forEach { it.emit(this) }

    // 4) Create CommandRequest
    addCommandSpecCreation(annotation["operation"]?.substringAfter(".") ?: "READ", keyParam)
}.build()

private fun List<CodeBlock>.join(separator: String): CodeBlock {
    if (isEmpty()) return CodeBlock.of("")
    val builder = CodeBlock.builder()
    forEachIndexed { index, part ->
        if (index > 0) {
            builder.add("%L", separator)
        }
        builder.add("%L", part)
    }
    return builder.build()
}

// ---------- WriteAction DSL ----------
private sealed class WriteAction {
    abstract val paramName: String
    abstract val nullable: Boolean
    abstract val slotCountExpr: CodeBlock
    abstract fun emitBody(code: CodeBlock.Builder)

    fun emit(code: CodeBlock.Builder) {
        if (nullable) {
            code.beginControlFlow("if (%L != null)", paramName)
            emitBody(code)
            code.endControlFlow()
        } else {
            emitBody(code)
        }
    }
}

private data class SimpleAction(
    override val paramName: String,
    private val writeCall: CodeBlock,
    override val nullable: Boolean,
) : WriteAction() {
    override val slotCountExpr =
        if (nullable) CodeBlock.of("(if (%L != null) 1 else 0)", paramName)
        else CodeBlock.of("1")

    override fun emitBody(code: CodeBlock.Builder) {
        code.addStatement("%L", writeCall)
    }
}

private data class CollectionAction(
    override val paramName: String,
    override val nullable: Boolean,
    private val elementBuilder: (String) -> WriteAction,
) : WriteAction() {
    override val slotCountExpr =
        if (nullable) CodeBlock.of("(%L?.size ?: 0)", paramName)
        else CodeBlock.of("%L.size", paramName)

    override fun emitBody(code: CodeBlock.Builder) {
        code.beginControlFlow("%L.forEach", paramName)
        elementBuilder("it").emitBody(code)
        code.endControlFlow()
    }
}

private data class SealedAction(
    override val paramName: String,
    override val nullable: Boolean,
    private val containerType: KSType,
    private val ctx: EncoderCtx,
    private val isVararg: Boolean,
) : WriteAction() {
    override val slotCountExpr: CodeBlock =
        if (nullable) CodeBlock.of("(if ($paramName != null) ${if (isVararg) "$paramName.size" else "1"} else 0)")
        else CodeBlock.of(if (isVararg) "$paramName.size" else "1", paramName)

    override fun emitBody(code: CodeBlock.Builder) {
        // Emit a when‐style block over all subclasses
        code.beginControlFlow("when (%L)", paramName)
        containerType.declaration.safeCast<KSClassDeclaration>()?.getSealedSubclasses()?.forEach { sub ->
            when {
                sub.isDataObject() -> {
                    ctx.fileSpec.addImport("eu.vendeli.rethis.utils", "writeStringArg")
                    code.beginControlFlow("is %T ->", sub.toClassName())
                    code.addStatement("buffer.writeStringArg(%L.toString(), charset)", paramName)
                    code.endControlFlow()
                    return@forEach
                }

                sub.hasAnnotation<RedisOptionContainer>() -> {
                    code.beginControlFlow("is %T ->", sub.toClassName())
                    code.endControlFlow()
                }

                else -> {
                    val fnName = createVariantHelper(sub, ctx)
                    code.beginControlFlow("is %T ->", sub.toClassName())
                    code.addStatement("buffer.%L(%L, charset)", fnName, paramName)
                    code.endControlFlow()
                }
            }
        }
        code.endControlFlow() // end of “when (paramName) { … }”
    }
}

private data class CompositeAction(
    override val paramName: String,
    override val nullable: Boolean,
    private val children: List<WriteAction>,
) : WriteAction() {
    override val slotCountExpr = if (nullable) CodeBlock.of("(if (%L != null) 1 else 0)", paramName)
    else CodeBlock.of("1")

    override fun emitBody(code: CodeBlock.Builder) {
        children.forEach { it.emit(code) }
    }
}

private data class ParamCtx(
    val origin: KSValueParameter,
    val nullable: Boolean,
    val isVararg: Boolean,
    val isCollection: Boolean,
)

// ----- Param → WriteAction -----
private fun buildActionForParam(
    param: KSValueParameter,
    ctx: EncoderCtx,
    effectiveName: String? = null,
): WriteAction {
    val name = effectiveName ?: param.name!!.asString()
    val type = param.type.resolve()
    val nullable = type.isMarkedNullable
    val isVararg = param.isVararg
    val isCollection = type.isCollection()

    val pCtx = ParamCtx(
        origin = param,
        nullable = nullable,
        isVararg = isVararg,
        isCollection = isCollection,
    )

    return when {
        isVararg || isCollection -> CollectionAction(name, nullable) { elemName ->
            buildActionForType(
                paramName = elemName,
                type = if (isVararg)
                    type
                else
                    type.arguments.single().type!!.resolve(),
                ctx = ctx,
                paramCtx = pCtx,
            )
        }

        else -> buildActionForType(name, type, ctx, pCtx)
    }
}

// ----- Type → WriteAction -----
@OptIn(KspExperimental::class)
private fun buildActionForType(
    paramName: String,
    type: KSType,
    ctx: EncoderCtx,
    paramCtx: ParamCtx,
): WriteAction = when {
    // standard primitives
    type.declaration.isStdType() -> {
        val additionalParams = buildList {
            when {
                type.declaration.isTimeType() -> {
                    ctx.fileSpec.addImport("eu.vendeli.rethis.api.spec.common.types", "TimeUnit")

                    add("TimeUnit.${type.getTimeUnit()}")
                }
            }
        }

        val token = paramCtx.origin.getAnnotationsByType(RedisOption.Token::class)
        if (type.declaration.isBool() && !token.none()) {
            ctx.fileSpec.addImport("eu.vendeli.rethis.utils", "writeStringArg")
            val tokenBlock = CodeBlock.builder().apply {
                token.forEach { add("if(%L) buffer.writeStringArg(%S, charset)", paramName, it.name) }
            }.build()

            SimpleAction(paramName, tokenBlock, paramCtx.nullable)
        } else SimpleAction(
            paramName,
            CodeBlock.of(
                "buffer.%L(%L, charset${additionalParams.joinToString(prefix = ", ")})",
                stdTypeFn(type, ctx),
                paramName,
            ),
            paramCtx.nullable,
        )
    }

    // enum / data-object
    type.declaration.isEnum() || type.declaration.isDataObject() -> SimpleAction(
        paramName,
        CodeBlock.of("buffer.writeStringArg(%L.toString(), charset)", paramName),
        paramCtx.nullable,
    ).also {
        ctx.fileSpec.addImport("eu.vendeli.rethis.utils", "writeStringArg")
    }

    // sealed structure
    type.declaration.isSealed() -> SealedAction(paramName, paramCtx.nullable, type, ctx, paramCtx.isVararg)

    // nested class
    else -> {
        val ctor = type.declaration.safeCast<KSClassDeclaration>()?.getConstructors()?.first()!!
        val children = ctor.parameters.map { p ->
            val nestedName = "$paramName.${p.name!!.asString()}"

            buildActionForParam(p, ctx, nestedName)
        }
        CompositeAction(paramName, paramCtx.nullable, children)
    }
}

// ----- Helpers -----
private fun stdTypeFn(type: KSType, ctx: EncoderCtx): String = type.declaration.simpleName.asString().let {
    "write${it}Arg"
}.also {
    ctx.fileSpec.addImport("eu.vendeli.rethis.utils", it)
}

@OptIn(KspExperimental::class)
private fun createVariantHelper(
    subclass: KSClassDeclaration,
    ctx: EncoderCtx,
): String {
    val parentName = subclass.safeCast<KSClassDeclaration>()
        ?.parent?.safeCast<KSClassDeclaration>()?.simpleName?.asString() ?: ""
    val variantName = subclass.simpleName.asString()
    val fnName = "write${parentName}${variantName}Arg"

    if (!ctx.addedExt.add(fnName)) {
        return fnName
    }

    // Build: private fun Buffer.fnName(value: Parent.Variant, charset: Charset) { … }
    val subtypeName = subclass.toClassName()
    val fnBuilder = FunSpec.builder(fnName)
        .receiver(Buffer::class)
        .addParameter("value", subtypeName)
        .addParameter("charset", charsetClassName)
        .addModifiers(KModifier.PRIVATE)

    // Inside, we must first write the token, then write all ctor fields
    val codeBlock = CodeBlock.builder()
    // 0) Create a buffer parameter
    codeBlock.addStatement("val buffer = this")

    // 1) write the Redis token from @RedisOption.Token annotation
    subclass.getAnnotationsByType(RedisOption.Token::class).also {
        if (!it.none()) {
            ctx.fileSpec.addImport("eu.vendeli.rethis.utils", "writeStringArg")
        }
    }.forEach {
        codeBlock.addStatement("buffer.writeStringArg(%S, charset)", it.name)
    }

    // 2) For each constructor parameter (non-nullable), write it
    subclass.primaryConstructor?.parameters?.forEach { p ->
        val fieldName = p.name!!.asString()
        val fieldType = p.type.resolve()
        val pCtx = ParamCtx(
            origin = p,
            nullable = false,
            isVararg = p.isVararg,
            isCollection = fieldType.isCollection(),
        )
        // Recurse to build a child write for “value.fieldName”
        val childAction = buildActionForType(
            paramName = "value.$fieldName",
            type = fieldType,
            ctx = ctx,
            paramCtx = pCtx,
        )
        childAction.emitBody(codeBlock)
    }

    fnBuilder.addCode(codeBlock.build())
    ctx.typeSpec.addFunction(fnBuilder.build())
    return fnName
}
