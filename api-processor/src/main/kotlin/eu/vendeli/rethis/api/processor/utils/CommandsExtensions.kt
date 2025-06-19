package eu.vendeli.rethis.api.processor.utils

import com.squareup.kotlinpoet.*
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RespCode

fun FileSpec.Builder.addCommandFunctions(
    codecName: String,
    commandName: String,
    parameters: Map<String, Pair<TypeName, List<KModifier>>>,
    type: TypeName,
    cmdPackagePart: String,
    responseTypes: Set<RespCode>,
) {
    val isNullable = RespCode.NULL in responseTypes
    addImport("eu.vendeli.rethis.codecs.$cmdPackagePart", codecName)
    addImport("eu.vendeli.rethis.core", "use")
    addFunction(
        FunSpec.builder(commandName.toCamelCase())
            .addModifiers(KModifier.SUSPEND)
            .receiver(ReThis::class).apply {
                parameters.map { p ->
                    ParameterSpec.builder(p.key, p.value.first, p.value.second).also {
                        if (p.value.first.isNullable) it.defaultValue("null")
                    }.build()
                }.let {
                    addParameters(it)
                }
            }
            .returns(type.copy(isNullable))
            .addCode(
                CodeBlock.builder().apply {
                    addStatement(
                        "val request = $codecName.encode(charset = cfg.charset${
                            parameters.entries.joinToString(prefix = ", ") {
                                "${it.key} = ${it.key}"
                            }
                        })",
                    )
                    beginControlFlow("return connectionPool.use")
                    addStatement("$codecName.decode(it.doRequest(request.buffer), cfg.charset)")
                    endControlFlow()
                }.build(),
            )
            .build(),
    )
}
