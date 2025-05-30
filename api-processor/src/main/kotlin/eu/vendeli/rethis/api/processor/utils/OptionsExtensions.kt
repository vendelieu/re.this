package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlinx.io.Buffer

internal fun TypeSpec.Builder.generateStatement(
    name: String,
    type: KSType,
    block: CodeBlock.Builder,
    isVararg: Boolean,
    fileSpec: FileSpec.Builder,
) {
    val finiteType = type.takeIf {
        it.arguments.isNotEmpty()
    }?.arguments?.joinToString {
        it.type?.resolve()?.declaration?.simpleName?.asString()!!
    } ?: type.toClassName().simpleName
    val typeName = finiteType.toPascalCase()
    val extName = "write${typeName}Arg"

    when {
        type.arguments.isNotEmpty() -> type.arguments.forEach { arg ->
            arg.type?.resolve()?.also {
                generateStatement(name, it, block, isVararg, fileSpec)
            }
        }

        type.declaration.isStdType() -> {
            fileSpec.addImport("eu.vendeli.rethis.utils", extName)

            block.addStatement(
                wrapStatement("buffer.$extName(%s, charset)", isVararg, type.isMarkedNullable, name),
            )
        }

        type.declaration.isEnum() -> block.addStatement(
            wrapStatement("buffer.writeStringArg(%s.name, charset)", isVararg, type.isMarkedNullable, name),
        )

        type.declaration.isDataObject() -> block.addStatement(
            wrapStatement("buffer.writeStringArg(%s.toString(), charset)", isVararg, type.isMarkedNullable, name),
        )

        type.declaration.isSealed() -> {
            block.addStatement("$name.forEach {\nwhen (it) {")
            type.declaration.safeCast<KSClassDeclaration>()?.getSealedSubclasses()?.forEach { d ->
                val cName = d.toClassName()
                val typeName = cName.simpleNames.joinToString(".")
                val funName = "write${typeName.toPascalCase()}Arg"

                if (!d.isStdType()) generateExtension(d, cName, funName, fileSpec)
                block.addStatement("\t\t\tis $typeName -> buffer.$funName(it, charset)")
            }
            block.addStatement("\t\t}")
            block.addStatement("}")
        }

        else -> type.declaration.safeCast<KSClassDeclaration>()?.getConstructors()?.forEach { constructor ->
            constructor.parameters.forEach { param ->
                generateStatement("${name}.${param.name!!.asString()}", param.type.resolve(), block, isVararg, fileSpec)
            }
        }
    }
}

@OptIn(KspExperimental::class)
private fun TypeSpec.Builder.generateExtension(
    d: KSClassDeclaration,
    typeName: ClassName,
    funName: String,
    fileSpec: FileSpec.Builder,
) {
    val fSpec = FunSpec.builder(funName)
        .addParameter("value", typeName)
        .addParameter("charset", charsetClassName)
        .addModifiers(KModifier.PRIVATE)
        .receiver(Buffer::class)

    val block = CodeBlock.builder().apply {
        addStatement("val buffer = this")
        d.getAnnotationsByType(RedisOption.Token::class).forEach {
            addStatement("buffer.writeStringArg(\"${it.name}\", charset)")
        }
        generateStatement("value", d.asStarProjectedType(), this, false, fileSpec)
    }

    addFunction(fSpec.addCode(block.build()).build())
}

private fun wrapStatement(
    statement: String,
    isVararg: Boolean,
    isMarkedNullable: Boolean,
    name: String,
) = when {
    isMarkedNullable && isVararg -> "$name?.forEach { ${statement.format("it")} }"
    isMarkedNullable -> "$name?.also { ${statement.format("it")} }"
    isVararg -> "$name.forEach { ${statement.format("it")} }"
    else -> statement.format(name)
}
