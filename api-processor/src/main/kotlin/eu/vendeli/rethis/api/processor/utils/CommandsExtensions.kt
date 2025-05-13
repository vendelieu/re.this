package eu.vendeli.rethis.api.processor.utils

import com.squareup.kotlinpoet.*
import eu.vendeli.rethis.ReThis

fun FileSpec.Builder.addCommandFunctions(
    codecName: String,
    commandName: String,
    parameters: Map<String, Pair<TypeName, List<KModifier>>>,
    type: TypeName,
    cmdPackagePart: String,
) {
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
            .returns(type.copy(true))
            .addCode(
                CodeBlock.builder().apply {
                    addStatement("val request = $codecName.encode(cfg.charset, ${parameters.keys.joinToString()})")
                    addStatement(
                        "return connectionPool.use {\n\tit.sendRequest(request.buffer)\n\t" +
                            "$codecName.decode(it.input, cfg.charset)\n}"
                    )
                }.build(),
            )
            .build(),
    )
}
