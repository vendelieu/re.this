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
    responseTypes: List<RespCode>,
) {
    val isNullable = RespCode.NULL in responseTypes
    addImport("eu.vendeli.rethis.codecs.$cmdPackagePart", codecName)
    addImport("eu.vendeli.rethis.core", "use")
    addFunction(
        FunSpec.builder(commandName.toCamelCase())
            .addModifiers(KModifier.SUSPEND)
            .receiver(ReThis::class).apply {
                parameters.forEach {
                    addParameter(it.key, it.value.first, it.value.second)
                }
            }
            .returns(type.copy(isNullable))
            .addCode(
                CodeBlock.builder().apply {
                    addStatement(
                        "val request = $codecName.encode(charset = cfg.charset${
                            parameters.entries.joinToString(prefix = ", ") {
                                "${it.key} = " + if (it.value.second.contains(KModifier.VARARG)) "*${it.key}" else it.key
                            }
                        })",
                    )
                    addStatement(
                        "return connectionPool.use {\n\t" +
                            "$codecName.decode(it.doRequest(request.buffer), cfg.charset)\n}",
                    )
                }.build(),
            )
            .build(),
    )
}
