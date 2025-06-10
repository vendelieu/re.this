package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlinx.io.Buffer

private fun RespCode.isString() = this == RespCode.SIMPLE_STRING || this == RespCode.BULK
internal fun TypeSpec.Builder.addDecodeFunction(
    respCode: List<RespCode>,
    specType: KSTypeArgument,
): TypeSpec.Builder {
    if (context.currentCommand.hasCustomDecoder) {
        return this
    }
    val type = specType.toTypeName()
    val isReturnBool = type.copy(true) == BOOLEAN.copy(true)
    val isReturnDouble = type.copy(true) == DOUBLE.copy(true)

    val isNullableResponse = RespCode.NULL in respCode
    val isImplicitMapResponse = RespCode.MAP in respCode && RespCode.ARRAY in respCode

    addImport(
        "eu.vendeli.rethis.api.spec.common.types.RespCode",
        "eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType",
    )

    addFunction(
        FunSpec.builder("decode")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("input", Buffer::class)
            .addParameter("charset", charsetClassName)
            .returns(type.copy(isNullableResponse))
            .addCode(
                CodeBlock.builder().apply {
                    addStatement("val code = RespCode.fromCode(input.readByte())")
                    beginControlFlow("return when(code)")
                    respCode.forEach { code ->
                        val arguments = specType.type?.resolve()?.let { t ->
                            t.takeIf {
                                it.arguments.isNotEmpty()
                            }?.arguments?.map { it.type?.resolve() } ?: listOf(t)
                        }?.filterNotNull()?.toTypedArray()!!
                        val tailStatement = when {
                            code.isString() && isReturnBool -> "== \"OK\""
                            code == RespCode.INTEGER && isReturnBool -> "== 1L"
                            code.isString() && isReturnDouble -> ".toDouble()"
                            else -> ""
                        }

                        beginControlFlow("RespCode.%L ->", code)

                        if (isImplicitMapResponse && code == RespCode.ARRAY) {
                            addImport("eu.vendeli.rethis.api.spec.common.decoders.ArrayMapDecoder")
                            addStatement("ArrayMapDecoder.decode<%s, %s>(input, charset, TYPE_INFO)".format(*arguments))
                        } else {
                            val decoder = decodersMap[code]!!
                            decoder.first?.let {
                                addImport("eu.vendeli.rethis.api.spec.common.decoders.$it")
                            }
                            addStatement(decoder.second.format(*arguments) + tailStatement)
                        }

                        endControlFlow()
                    }
                    beginControlFlow("else ->")
                    addStatement("throw UnexpectedResponseType(\"Expected $respCode but got \$code\")")
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
    val sizePart = if (
        parameters.any { it.hasAnnotation<RedisOptional>() || it.type.resolve().isMarkedNullable }
    ) "" else "*$size"
    return "$sizePart\\r\\n$commandPart\\r\\n"
}

internal fun buildStaticHeaderInitializer(header: String): CodeBlock = CodeBlock.Builder().apply {
    beginControlFlow("Buffer().apply {")
    addStatement("writeString(\"%L\")", header)
    endControlFlow()
}.build()

internal fun CodeBlock.Builder.addCommandSpecCreation(
    operationName: String,
) {
    addStatement(
        """
            return CommandRequest(
                buffer = buffer,
                operation = %T.%L,
                typeInfo = TYPE_INFO,
                isBlocking = BLOCKING_STATUS,
            )
            """.trimIndent(),
        RedisOperation::class, operationName,
    )
}
