package eu.vendeli.rethis.api.processor.utils

import com.squareup.kotlinpoet.*
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RespCode

fun FileSpec.Builder.addCommandFunctions(
    codecName: String,
    parameters: Map<String, Pair<TypeName, List<KModifier>>>,
    type: TypeName,
    cmdPackagePart: String,
    responseTypes: Set<RespCode>,
) {
    val isNullable = RespCode.NULL in responseTypes
    addImport("eu.vendeli.rethis.codecs.$cmdPackagePart", codecName)
    addFunction(
        FunSpec.builder(codecName.removeSuffix("CommandCodec").replaceFirstChar { it.lowercase() })
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
                    val parameters = parameters.entries.joinToString(prefix = ", ") {
                        "${it.key} = ${it.key}"
                    }
                    addImport("eu.vendeli.rethis.topology", "handle")
                    beginControlFlow("val request = if(cfg.withSlots)")
                    addStatement("$codecName.encodeWithSlot(charset = cfg.charset$parameters)")
                    nextControlFlow("else")
                    addStatement("$codecName.encode(charset = cfg.charset$parameters)")
                    endControlFlow()
                    addStatement("return $codecName.decode(topology.handle(request), cfg.charset)")
                }.build(),
            )
            .build(),
    )
}
