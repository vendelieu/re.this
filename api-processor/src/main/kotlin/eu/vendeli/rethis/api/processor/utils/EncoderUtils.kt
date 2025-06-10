package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlinx.io.Buffer
import sun.util.locale.UnicodeLocaleExtension.isKey

// todo keys

// todo check size (nested elements may have also size)

@OptIn(KspExperimental::class)
internal fun addEncoderCode() {
    // Collect helpers needed for sealed variants
    val helpersNeeded = mutableMapOf<String, KSClassDeclaration>()

    val encodeCode = CodeBlock.builder()
    addImport(
        "kotlinx.io.Buffer",
        "kotlinx.io.writeString",
        "eu.vendeli.rethis.api.spec.common.utils.CRC16",
        "eu.vendeli.rethis.api.spec.common.types.CommandRequest",
        "eu.vendeli.rethis.api.spec.common.types.RedisOperation",
        "io.ktor.util.reflect.TypeInfo",
        "io.ktor.util.reflect.typeInfo",
        "io.ktor.utils.io.core.toByteArray",
    )

    val specSigArguments = context.currentCommand.encodeFunction.parameters.associate { param ->
        param.name!!.asString() to Pair(
            param.type.resolve().toTypeName(),
            listOfNotNull(if (param.isVararg) KModifier.VARARG else null),
        )
    }

    val rawActions = context.currentCommand.encodeFunction.parameters.map { param ->
        buildActionForParam(param, helpersNeeded)
    }
    val actions = rawActions.sortedWith(
        Comparator { a, b ->
            lexCompare(a.rSpecPath, b.rSpecPath)
        },
    )

    encodeCode.addStatement("val buffer = Buffer()")

    if (actions.any { it.nullable }) {
        val sizeExpr = actions.map { it.slotCountExpr }.joinToString(" + ") { "%L" }
        encodeCode.addStatement(
            "val size = %L",
            CodeBlock.of(sizeExpr, *actions.map { it.slotCountExpr }.toTypedArray()),
        )
        encodeCode.addStatement("buffer.writeString(\"\$size\")")
    }
    encodeCode.addStatement("COMMAND_HEADER.copyTo(buffer)")

    actions.forEach { it.emit(encodeCode) }

    encodeCode.addCommandSpecCreation(context.currentCommand.command.operation.name)

    context.typeSpec.addFunction(
        FunSpec.builder("encode")
            .addModifiers(KModifier.SUSPEND)
            .apply {
                addParameter("charset", charsetClassName)
                specSigArguments.forEach { param ->
                    addParameter(param.key, param.value.first, param.value.second)
                }
            }
            .returns(commandRequestClassName)
            .addCode(encodeCode.build())
            .build(),
    )

    // 6) Now generate encodeWithSlot(...)
    val slotBody = CodeBlock.builder().apply {
        val requestStatement = "encode(charset${
            specSigArguments.entries.joinToString(prefix = ", ") {
                "${it.key} = " + if (it.value.second.contains(KModifier.VARARG)) "*${it.key}" else it.key
            }
        })"

        if (context.currentRSpec.allArguments.all { it.keySpecIndex == null }) {
            addStatement("return %L", requestStatement)
            return@apply
        }

        addStatement("val slotSet = mutableSetOf<Int>()")
        actions.forEach { it.emitSlot(this) }
        addStatement(
            "if (slotSet.size > 1) throw IllegalArgumentException(%S)",
            "Cross‐slot operations are not supported",
        )
        addStatement("val slot = slotSet.firstOrNull()")
        addStatement("val request = %L", requestStatement)

        addStatement(
            "return if (slot == null) request else request.withKey(slot)",
        )
    }.build()

    context.typeSpec.addFunction(
        FunSpec.builder("encodeWithSlot")
            .addModifiers(KModifier.SUSPEND, KModifier.PUBLIC)
            .apply {
                addParameter("charset", charsetClassName)
                specSigArguments.forEach { param ->
                    addParameter(param.key, param.value.first, param.value.second)
                }
            }
            .addCode(slotBody)
            .returns(commandRequestClassName)
            .build(),
    )

    for ((fnName, subclass) in helpersNeeded) {
        val fnBuilder = FunSpec.builder(fnName)
            .receiver(Buffer::class)
            .addParameter("value", subclass.toClassName())
            .addParameter("charset", charsetClassName)
            .addModifiers(KModifier.PRIVATE)

        val body = CodeBlock.builder().apply {
            addStatement("val buffer = this")

            // 2) for each constructor param, inline write logic
            writeExtArguments(subclass, helpersNeeded)
        }.build()

        context.typeSpec.addFunction(fnBuilder.addCode(body).build())
    }
}

private fun CodeBlock.Builder.writeExtArguments(
    c: KSClassDeclaration,
    helpersNeeded: MutableMap<String, KSClassDeclaration>,
) {
    val ctor = c.primaryConstructor!!

    if (ctor.parameters.size == 1 && ctor.parameters.first().isVararg) {
        c.primaryConstructor!!.takeIf {
            it.parameters.size == 1 && it.parameters.first().isVararg
        }?.parameters?.first()?.also { p ->
            val rArg = context.currentRSpec.allArguments.find { it.name == p.name?.asString() }
            val fieldName = p.name!!.asString()
            val fieldAccess = "value.$fieldName"
            val fieldType = p.type.resolve()

            if (rArg?.multipleToken == false) writeTokensFor(c)
            beginControlFlow("%L.forEach {", fieldAccess)
            if (rArg?.multipleToken == true) writeTokensFor(c)
            inferWriting(fieldType, "it", helpersNeeded)
            endControlFlow()
        }

        return
    }

    writeTokensFor(c)
    c.primaryConstructor!!.parameters.forEach { p ->
        val fieldName = p.name!!.asString()
        val fieldAccess = "value.$fieldName"
        val fieldType = p.type.resolve()
        // normal single field
        inferWriting(fieldType, fieldAccess, helpersNeeded)
    }
    if (c.isDataObject()) {
        addStatement("buffer.writeStringArg(value.toString(), charset)")
    }
}

@OptIn(KspExperimental::class)
private fun CodeBlock.Builder.writeTokensFor(c: KSClassDeclaration) {
    c.getAnnotationsByType(RedisOption.Token::class).forEach { tok ->
        addStatement("buffer.writeStringArg(%S, charset)", tok.name)
    }
}

private fun CodeBlock.Builder.inferWriting(
    fieldType: KSType,
    fieldAccess: String,
    helpersNeeded: MutableMap<String, KSClassDeclaration>,
) {
    when {
        fieldType.declaration.isStdType() -> {
            // primitives: use your existing writeXxxArg
            val fn = stdTypeFn(fieldType)
            addImport("eu.vendeli.rethis.utils.$fn")
            val additionalParams = buildList {
                when {
                    fieldType.declaration.isTimeType() -> {
                        addImport("eu.vendeli.rethis.api.spec.common.types.TimeUnit")
                        add("TimeUnit.${fieldType.getTimeUnit()}")
                    }
                }
            }.joinToString(prefix = ", ")
            addStatement("buffer.%L(%L, charset$additionalParams)", fn, fieldAccess)
        }

        fieldType.declaration.isEnum() || fieldType.declaration.isDataObject() -> {
            addImport("eu.vendeli.rethis.utils.writeStringArg")
            addStatement("buffer.writeStringArg(%L.toString(), charset)", fieldAccess)
        }

        else -> {
            // nested complex: recurse into helper
            val nestedClass = fieldType.declaration.safeCast<KSClassDeclaration>()!!
            val nestedFn = registerVariantHelper(nestedClass, helpersNeeded)
            addStatement("buffer.%L(%L, charset)", nestedFn, fieldAccess)
        }
    }
}

private fun findPathInRSpecTree(paramName: String): List<Int> {
    // flatten the RSpec tree (root nodes + all children)
    val all = context.rSpecTree.flatMap { it.children } + context.rSpecTree
    val target = all.firstOrNull { it.normalizedName == paramName.normalizeParam() }
    return target?.path ?: emptyList()
}

private fun buildActionForParam(
    param: KSValueParameter,
    helpersNeeded: MutableMap<String, KSClassDeclaration>,
): WriteAction {
    val name = param.name!!.asString()
    // 2a) try the NodeLink
    val rnode = context.nodeLink.entries
        .firstOrNull { it.key.symbol == param }
        ?.value

    // 2b) fallback to name lookup in the raw RSpec tree
    val path = rnode?.path ?: findPathInRSpecTree(name)
    val type = param.type.resolve()
    val pCtx = ParamCtx(
        origin = param,
        nullable = type.isMarkedNullable,
        isVararg = param.isVararg,
        isCollection = type.isCollection(),
        helpersNeeded = helpersNeeded,
        path = path,
    )

    return if (param.isVararg || pCtx.isCollection) {
        CollectionAction(name, pCtx.nullable, path) { elem ->
            buildActionForType(elem, param.type.collectionAwareType(), pCtx)
        }
    } else {
        buildActionForType(name, type, pCtx)
    }
}

@OptIn(KspExperimental::class)
private fun buildActionForType(
    paramName: String,
    type: KSType,
    paramCtx: ParamCtx,
): WriteAction = when {
    // primitives
    type.declaration.isStdType() -> {
        val isKey = context.libSpecTree.findParameterByName(paramCtx.origin.name!!.asString())?.let {
            findPairNode(it)?.safeCast<RSpecNode.Simple>()?.keyIdx != null
        } ?: false
        val tokens = paramCtx.origin.getAnnotationsByType(RedisOption.Token::class)
        if (type.declaration.isBool() && tokens.any()) {
            addImport("eu.vendeli.rethis.utils.writeStringArg")
            val block = CodeBlock.builder().apply {
                tokens.forEach { t -> add("if (%L) buffer.writeStringArg(%S, charset)", paramName, t.name) }
            }.build()

            SimpleAction(
                paramName = paramName,
                writeCall = block,
                nullable = paramCtx.nullable,
                rSpecPath = paramCtx.path,
                isKey = isKey,
            )
        } else {
            val fn = stdTypeFn(type)
            addImport("eu.vendeli.rethis.utils.$fn")
            val additionalParams = buildList {
                when {
                    type.declaration.isTimeType() -> {
                        addImport("eu.vendeli.rethis.api.spec.common.types.TimeUnit")
                        add("TimeUnit.${type.getTimeUnit()}")
                    }
                }
            }.joinToString(prefix = ", ")
            val call = CodeBlock.of("buffer.%L(%L, charset$additionalParams)", fn, paramName)

            SimpleAction(
                paramName = paramName,
                writeCall = call,
                nullable = paramCtx.nullable,
                rSpecPath = paramCtx.path,
                isKey = isKey,
            )
        }
    }
    // enum or object
    type.declaration.isEnum() || type.declaration.isDataObject() -> {
        addImport("eu.vendeli.rethis.utils.writeStringArg")
        val call = CodeBlock.of("buffer.writeStringArg(%L.toString(), charset)", paramName)
        SimpleAction(paramName, call, paramCtx.nullable, paramCtx.path)
    }
    // sealed
    type.declaration.isSealed() -> {
        SealedAction(paramName, paramCtx.nullable, type, paramCtx.isVararg, paramCtx.path, paramCtx.helpersNeeded)
    }
    // nested class
    else -> {
        val ctor = type.declaration.safeCast<KSClassDeclaration>()!!.primaryConstructor!!
        val children = ctor.parameters.mapIndexed { idx, p ->
            val childName = "$paramName.${p.name!!.asString()}"
            val childType = p.type.resolve()

            buildActionForType(
                childName, childType,
                ParamCtx(
                    origin = p,
                    nullable = childType.isMarkedNullable,
                    isVararg = p.isVararg,
                    isCollection = childType.isCollection(),
                    helpersNeeded = paramCtx.helpersNeeded,
                    path = paramCtx.path + idx,
                ),
            )
        }

        CompositeAction(paramName, paramCtx.nullable, children, paramCtx.path)
    }
}

private data class ParamCtx(
    val origin: KSValueParameter,
    val nullable: Boolean,
    val isVararg: Boolean,
    val isCollection: Boolean,
    val helpersNeeded: MutableMap<String, KSClassDeclaration>,
    val path: List<Int>,
)

private fun stdTypeFn(type: KSType): String =
    "write${type.declaration.simpleName.asString()}Arg"

private fun lexCompare(a: List<Int>, b: List<Int>): Int {
    val min = minOf(a.size, b.size)
    for (i in 0 until min) {
        val diff = a[i].compareTo(b[i])
        if (diff != 0) return diff
    }
    return a.size.compareTo(b.size)
}

internal fun registerVariantHelper(
    subclass: KSClassDeclaration,
    helpersNeeded: MutableMap<String, KSClassDeclaration>,
): String {
    val parentName = subclass.parent.safeCast<KSClassDeclaration>()?.simpleName?.asString().orEmpty()
    val variantName = subclass.simpleName.asString()
    val fnName = "write${parentName}${variantName}Arg"

    // record need
    helpersNeeded.putIfAbsent(fnName, subclass)
    return fnName
}
