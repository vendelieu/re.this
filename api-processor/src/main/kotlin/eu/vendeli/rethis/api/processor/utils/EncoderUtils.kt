package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.core.LibTreePlanter
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.EncodePhase
import eu.vendeli.rethis.api.processor.types.WriteOpProps
import eu.vendeli.rethis.api.processor.types.emitOp
import eu.vendeli.rethis.api.processor.types.filterWithoutKey

@OptIn(KspExperimental::class)
internal fun addEncoderCode() {
    val encodeCode = CodeBlock.builder()
    addImport(
        "eu.vendeli.rethis.shared.utils.CRC16",
        "eu.vendeli.rethis.shared.types.CommandRequest",
        "eu.vendeli.rethis.shared.types.RedisOperation",
    )

    val specSigArguments = context.currentCommand.encodeFunction.parameters.associate { param ->
        param.name!!.asString() to Pair(
            param.type.resolve().toTypeName(),
            listOfNotNull(if (param.isVararg) KModifier.VARARG else null),
        )
    }

    LibTreePlanter.prepare()
    val ops = buildWritePlan(context.enrichedTree)

    if (!context.currentCommand.hasCustomEncoder) {
        val baseSize = context.currentCommand.command.name.split(' ').size
        encodeCode.addStatement("var argCount = $baseSize")

        context += CodeGenContext(encodeCode)
        ops.forEach { it.emitOp(EncodePhase.SIZE) }

        addImport("kotlinx.io.Buffer")
        encodeCode.addStatement("val buffer = Buffer()")
        if (context.currentCommand.haveVaryingSize) {
            addImport("eu.vendeli.rethis.utils.writeArrayHeader")
            encodeCode.addStatement("buffer.writeArrayHeader(argCount)")
        }
        encodeCode.addStatement("buffer.write(COMMAND_HEADER)")

        ops.forEach { it.emitOp(EncodePhase.WRITE) }

        encodeCode.addCommandSpecDeclaration()
    } else {
        encodeCode.addStatement("return %L", context.currentCommand.customCodec!!.encoder)
    }

    context.typeSpec.addFunction(
        FunSpec.builder("encode")
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

    val slotBody = CodeBlock.builder().apply {
        val requestStatement = "encode(charset${
            specSigArguments.entries.joinToString(prefix = ", ") {
                "${it.key} = ${it.key}"
            }
        })"

        if (context.currentRSpec.allArguments.all { it.keySpecIndex == null }) {
            addStatement("return %L", requestStatement)
            return@apply
        }
        addImport("eu.vendeli.rethis.shared.utils.validateSlot")

        addStatement("var slot: Int? = null")
        context += CodeGenContext(this)
        val slotOps = ops.mapNotNull { it.filterWithoutKey() }
        slotOps.forEach { it.emitOp(EncodePhase.SLOT) }

        slotOps.findWrappedCall { it.props.contains(WriteOpProps.COLLECTION) }?.also {
            addImport("eu.vendeli.rethis.shared.types.KeyAbsentException")
            addStatement("if (slot == null) throw KeyAbsentException(\"Expected key is not provided\")")
        }

        addStatement("val request = %L", requestStatement)
        addStatement(
            "return request.withSlot(slot %% 16384)",
        )
    }.build()

    context.typeSpec.addFunction(
        FunSpec.builder("encodeWithSlot")
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
}
