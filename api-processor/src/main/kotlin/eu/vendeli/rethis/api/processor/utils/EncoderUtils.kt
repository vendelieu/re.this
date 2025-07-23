package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.context.CodeGenContext
import eu.vendeli.rethis.api.processor.core.LibTreePlanter
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.WriteOp
import eu.vendeli.rethis.api.processor.types.WriteOpProps
import eu.vendeli.rethis.api.processor.types.emitOp
import eu.vendeli.rethis.api.processor.types.filterWithoutKey

@OptIn(KspExperimental::class)
internal fun addEncoderCode() {
    val encodeCode = CodeBlock.builder()
    addImport(
        "kotlinx.io.Buffer",
        "kotlinx.io.writeString",
        "eu.vendeli.rethis.api.spec.common.utils.CRC16",
        "eu.vendeli.rethis.api.spec.common.types.CommandRequest",
        "eu.vendeli.rethis.api.spec.common.types.RedisOperation",
        "io.ktor.utils.io.core.toByteArray",
    )

    val specSigArguments = context.currentCommand.encodeFunction.parameters.associate { param ->
        param.name!!.asString() to Pair(
            param.type.resolve().toTypeName(),
            listOfNotNull(if (param.isVararg) KModifier.VARARG else null),
        )
    }

    LibTreePlanter.prepare()
    val root = context.enrichedTree
    val ops = buildWritePlan(root)

    if (!context.currentCommand.hasCustomEncoder) {
        encodeCode.addStatement("va%L buffer = Buffer()", if (context.currentCommand.haveVaryingSize) "r" else "l")

        if (context.currentCommand.haveVaryingSize) {
            val baseSize = context.currentCommand.command.name.split(' ').size
            encodeCode.addStatement("var size = $baseSize")
        }
        encodeCode.addStatement("COMMAND_HEADER.copyTo(buffer)")

        context += CodeGenContext(encodeCode)
        ops.forEach { it.emitOp(encode = true) }

        encodeCode.addCommandSpecDeclaration()
    } else {
        encodeCode.addStatement("return %L", context.currentCommand.customCodec!!.encoder)
    }

    context.typeSpec.addFunction(
        FunSpec.builder("encode")
            .addModifiers(KModifier.SUSPEND)
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
        addImport("eu.vendeli.rethis.api.spec.common.utils.validateSlot")

        addStatement("var slot: Int? = null")
        context += CodeGenContext(this)
        val slotOps = ops.mapNotNull { it.filterWithoutKey() }
        slotOps.forEach { it.emitOp(encode = false) }

        val collectionCheck: (WriteOp) -> Boolean = {
            it is WriteOp.WrappedCall && it.props.contains(WriteOpProps.COLLECTION)
        }
        slotOps.singleOrNull { op ->
            collectionCheck(op) || op is WriteOp.WrappedCall
                && op.inner.singleOrNull { collectionCheck(it) } != null
        }?.also {
            addImport("eu.vendeli.rethis.api.spec.common.types.KeyAbsentException")
            addStatement("if(slot == null) throw KeyAbsentException(\"Expected key is not provided\")")
        }

        addStatement("val request = %L", requestStatement)
        addStatement(
            "return request.withSlot(slot %% 16384)",
        )
    }.build()

    context.typeSpec.addFunction(
        FunSpec.builder("encodeWithSlot")
            .addModifiers(KModifier.SUSPEND, KModifier.INLINE)
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
