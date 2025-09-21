package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.Buffer

internal fun TypeSpec.Builder.addDecodeFunction(
    respCode: Set<RespCode>,
    specType: KSTypeArgument,
): TypeSpec.Builder {
    val type = specType.toTypeName()
    val isNullableResponse = RespCode.NULL in respCode

    if (context.currentCommand.hasCustomDecoder) {
        val customDecoder = context.currentCommand.customCodec?.runCatching {
            decoder
        }?.getOrNull() ?: return this

        FunSpec.builder("decode")
            .addParameter("input", Buffer::class)
            .addParameter("charset", charsetClassName)
            .addModifiers(KModifier.SUSPEND)
            .returns(type.copy(isNullableResponse))
            .addCode(
                CodeBlock.builder().apply {
                    if (customDecoder.qualifiedName == ResponseDecoder::class.qualifiedName) {
                        addStatement("return TODO()")
                        return@apply
                    }
                    addImport(customDecoder.qualifiedName!!)
                    addStatement("return %L.decode(input, charset)", customDecoder.simpleName)
                }.build(),
            )
            .build().also {
                addFunction(it)
            }
        return this
    }

    addImport(
        "eu.vendeli.rethis.shared.types.RespCode",
        "eu.vendeli.rethis.shared.types.UnexpectedResponseType",
    )

    addFunction(
        FunSpec.builder("decode")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("input", Buffer::class)
            .addParameter("charset", charsetClassName)
            .returns(type.copy(isNullableResponse))
            .addCode(
                CodeBlock.builder().apply {
                    if (respCode.isEmpty()) return@apply
                    addImport("eu.vendeli.rethis.utils.parseCode")
                    addStatement("val code = input.parseCode(RespCode.%L)", respCode.first())

                    beginControlFlow("return when(code)")
                    respCode.forEach { writeDecoder(it) }
                    beginControlFlow("else ->")
                    addImport("eu.vendeli.rethis.shared.utils.tryInferCause")
                    addStatement($$"throw UnexpectedResponseType(\"Expected $$respCode but got $code\", input.tryInferCause(code))")
                    endControlFlow()

                    endControlFlow()
                }.build(),
            )
            .build(),
    )

    return this
}

internal fun buildStaticCommandParts(
    vararg mainCommandPart: String,
    parameters: List<KSValueParameter>,
): String {
    val size = mainCommandPart.size + parameters.size
    val commandPart = mainCommandPart.joinToString("\\r\\n") { "$${it.length}\\r\\n$it" }
    val sizePart = if (context.currentCommand.haveVaryingSize) "" else "*$size"
    return "$sizePart\\r\\n$commandPart\\r\\n"
}

internal fun buildStaticHeaderInitializer(header: String): CodeBlock = CodeBlock.Builder().apply {
    beginControlFlow("Buffer().apply")
    addStatement("writeString(\"%L\")", header)
    endControlFlow()
}.build()

internal fun CodeBlock.Builder.addCommandSpecDeclaration() {
    addStatement("")
    if (context.currentCommand.haveVaryingSize) {
        beginControlFlow("buffer = Buffer().apply")
        addStatement($$"writeString(\"*$size\")")
        addStatement("transferFrom(buffer)")
        endControlFlow()
    }
    addStatement(
        "return CommandRequest(buffer, %T.%L, BLOCKING_STATUS)",
        RedisOperation::class,
        context.currentCommand.command.operation.name,
    )
}
