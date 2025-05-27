package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
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
            .addCode(fileSpec.buildEncoderCode(annotation, params, keyParam))
            .build(),
    )
    return this
}

internal fun TypeSpec.Builder.addDecodeFunction(
    respCode: List<RespCode>,
    type: TypeName,
): TypeSpec.Builder {
    val isReturnBool = type.copy(true) == BOOLEAN.copy(true)
    val isNullableResponse = RespCode.NULL in respCode

    addFunction(
        FunSpec.builder("decode")
            .addModifiers(KModifier.SUSPEND)
            .addParameter("input", Buffer::class)
            .addParameter("charset", charsetClassName)
            .returns(type.copy(isNullableResponse))
            .addCode(
                CodeBlock.builder().apply {
                    addStatement("val code = RespCode.fromCode(input.readByte())")
                    addStatement(
                        "return when(code) {\n\t${
                            respCode.joinToString("\n\t") {
                                val decoderTail = when {
                                    it == RespCode.SIMPLE_STRING && isReturnBool -> " == \"OK\""
                                    it == RespCode.INTEGER && isReturnBool -> " == 1L"

                                    else -> ""
                                }
                                "RespCode.$it -> " + decodersMap[it]!!.second + decoderTail
                            }
                        }\n\telse -> throw UnexpectedResponseType(\"Expected $respCode but got \$code\")\n}",
                    )

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

private fun FileSpec.Builder.buildEncoderCode(
    annotation: Map<String, String>,
    parameters: List<KSValueParameter>,
    keyParam: KSValueParameter?,
): CodeBlock = CodeBlock.builder().apply {
    var isThereOptionals = false
    if (parameters.isEmpty()) {
        addStatement("val buffer = COMMAND_HEADER")
    } else {
        addStatement("val buffer = %T()", Buffer::class)
        var stablePartsSize = annotation["name"]!!.split(" ").size
        val optionalsSize = StringBuilder()
        parameters.forEach {
            if (!it.hasAnnotation<RedisOptional>() && !it.type.resolve().isMarkedNullable) {
                stablePartsSize++
            } else {
                isThereOptionals = true
                optionalsSize.append("if (${it.name!!.asString()} != null) { size++ }\n")
            }
        }
        if (isThereOptionals) {
            addStatement("var size = $stablePartsSize")
            addStatement(optionalsSize.toString())
            addStatement("buffer.writeString(\"\$size\")")
        }

        addStatement("COMMAND_HEADER.copyTo(buffer)\n")
    }

    parameters.forEach { param ->
        when {
            param.hasAnnotation<RedisOptional>() ->
                addStatement("%N?.also { ${typeWrite(param, "it")} }", param.name!!.asString())

            param.isVararg -> addStatement(
                "buffer.writeVarargs${
                    param.type.resolve().arguments.first().type!!.resolve().toClassName().simpleName
                }(%N)",
                param.name!!.asString(),
            )

            else -> addStatement(typeWrite(param, param.name!!.asString()))
        }
    }

    addCommandSpecCreation(annotation["operation"]?.substringAfter(".") ?: "READ", keyParam)
}.build()


private fun CodeBlock.Builder.addCommandSpecCreation(
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
