package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlinx.io.Buffer

internal fun TypeSpec.Builder.addEncodeFunction(
    fileSpec: FileSpec.Builder,
    annotation: Map<String, String>,
    parameters: Map<String, Pair<TypeName, List<KModifier>>>,
    params: List<KSValueParameter>,
    keyParam: KSValueParameter?,
): TypeSpec.Builder {
    addFunction(
        FunSpec.builder("encode")
            .addModifiers(KModifier.SUSPEND)
            .apply {
                addParameter(
                    "charset",
                    charsetClassName,
                )
                parameters.forEach { param ->
                    addParameter(param.key, param.value.first, param.value.second)
                }
            }
            .returns(
                CommandRequest::class.asClassName().parameterizedBy(
                    keyParam?.type?.resolve()?.toTypeName() ?: NOTHING,
                ),
            )
            .addCode(fileSpec.buildEncoderCode(this, annotation, params, keyParam))
            .build(),
    )
    return this
}

private fun RespCode.isString() = this == RespCode.SIMPLE_STRING || this == RespCode.BULK
internal fun TypeSpec.Builder.addDecodeFunction(
    respCode: List<RespCode>,
    specType: KSTypeArgument,
): TypeSpec.Builder {
    val type = specType.toTypeName()
    val isReturnBool = type.copy(true) == BOOLEAN.copy(true)
    val isReturnDouble = type.copy(true) == DOUBLE.copy(true)

    val isNullableResponse = RespCode.NULL in respCode
    val isImplicitMapResponse = RespCode.MAP in respCode && RespCode.ARRAY in respCode

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
                            addStatement("ArrayMapDecoder.decode<%s, %s>(input, charset, TYPE_INFO)".format(*arguments))
                        } else {
                            addStatement(decodersMap[code]!!.second.format(*arguments) + tailStatement)
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

internal fun buildStaticHeaderInitializer(header: String): String {
    return "Buffer().apply {\n" +
        "\twriteString(\"${header}\")" +
        "\n}"
}

internal fun CodeBlock.Builder.addCommandSpecCreation(
    operationName: String,
    keyParam: KSValueParameter?,
) {
    if (keyParam != null) {
        addStatement(
            """
            return CommandRequest<%T>(
                buffer = buffer,
                operation = %T.%L,
                typeInfo = TYPE_INFO,
                isBlocking = BLOCKING_STATUS,
            ).withKey(%N)
            """.trimIndent(),
            keyParam.type.toTypeName(), RedisOperation::class, operationName, keyParam.name!!.asString(),
        )
    } else {
        addStatement(
            """
            return CommandRequest.keyless(
                buffer = buffer,
                operation = %T.%L,
                typeInfo = TYPE_INFO,
                isBlocking = BLOCKING_STATUS,
            )
            """.trimIndent(),
            RedisOperation::class, operationName,
        )
    }
}
