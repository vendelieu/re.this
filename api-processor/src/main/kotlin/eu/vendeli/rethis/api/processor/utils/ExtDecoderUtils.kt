package eu.vendeli.rethis.api.processor.utils

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.shared.decoders.aggregate.SetStringDecoder
import eu.vendeli.rethis.shared.decoders.general.BulkByteStringDecoder
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RespCode

private const val DECODE_STRING = "%L.decode(input, charset, code)"
private const val DECODE_STRING_N = "%L.decodeNullable(input, charset, code)"
private val String.name get() = substringAfterLast('.')
private fun currName() = context.currentCommand.klass.qualifiedName?.asString()

internal fun CodeBlock.Builder.writeDecoder(code: RespCode) {
    beginControlFlow("RespCode.%L ->", code.name)

    val currCmd = context.currentCommand
    val respCode = currCmd.command.responseTypes

    val isImplicitMapResponse = RespCode.MAP in respCode && RespCode.ARRAY in respCode

    when {
        isImplicitMapResponse && code == RespCode.ARRAY -> {
            val baseArg = currCmd.arguments.last().toClassName()
            val argument = baseArg.copy(false)
            val decoder = mapDecoders[argument] ?: throw ReThisException(
                "Unsupported type for map decoder: $baseArg [${currName()}]",
            )
            addImport(decoder)

            val baseStatement = if (baseArg.isNullable) DECODE_STRING_N else DECODE_STRING
            addStatement(baseStatement, decoder.name)
        }

        code.isSimple -> writePlainDecoder(code)

        code == RespCode.ARRAY || code == RespCode.SET -> writeCollectionDecoder(code)

        code == RespCode.MAP -> writeMapDecoder()

        else -> throw UnsupportedOperationException("Unsupported response type: $code")
    }

    endControlFlow()
}

private fun RespCode.isString() = this == RespCode.SIMPLE_STRING || this == RespCode.BULK
internal fun CodeBlock.Builder.writePlainDecoder(code: RespCode) {
    if (code == RespCode.NULL) {
        addStatement("null")
        return
    }
    val baseType = context.currentCommand.type
    val cType = baseType.copy(true)
    val isReturnBool = BOOLEAN.copy(true) == cType
    val isReturnDouble = DOUBLE.copy(true) == cType
    val isReturnByteString = BYTESTRING.copy(true) == cType

    val decoder = when {
        isReturnByteString && code == RespCode.BULK -> BulkByteStringDecoder::class.qualifiedName!!
        else -> plainDecoders[code] ?: throw ReThisException("Unsupported response type for plain decoder: ${code.name} [${currName()}]")
    }
    addImport(decoder)

    val statement = StringBuilder()
    val baseStatement = when {
        code == RespCode.BULK && context.currentCommand.command.responseTypes.contains(RespCode.NULL) -> DECODE_STRING_N
        baseType.isNullable -> DECODE_STRING_N
        else -> DECODE_STRING
    }
    statement.append(baseStatement)


    when {
        code.isString() && isReturnBool -> statement.append(" == \"OK\"")
        code == RespCode.INTEGER && isReturnBool -> statement.append(" == 1L")
        code != RespCode.BULK && isReturnByteString -> {
            context.fileSpec.addImport("kotlinx.io.bytestring", "encodeToByteString")
            statement.append(".encodeToByteString()")
        }
        code.isString() && isReturnDouble -> {
            if (statement[10] == DECODE_STRING_N[10]) statement.append("?")
            statement.append(".toDouble()")
        }
    }

    addStatement(statement.toString(), decoder.name)
}

internal fun CodeBlock.Builder.writeCollectionDecoder(code: RespCode) {
    val currentCmd = context.currentCommand
    val baseArg = currentCmd.arguments.first().toTypeName()
    val baseStatement = if (baseArg.isNullable) DECODE_STRING_N else DECODE_STRING
    val argument = baseArg.copy(false)
    val respCode = context.currentCommand.command.responseTypes

    if (RespCode.SET in respCode && RespCode.ARRAY == code) {
        val decoder = SetStringDecoder::class.qualifiedName!!
        addImport(decoder)
        addStatement(baseStatement, decoder.name)
        return
    }

    val decoderOptions = collectionDecoders[code]
        ?: throw ReThisException("Unsupported response type for collection decoder: ${code.name} [${currName()}]")
    val decoder =
        decoderOptions[argument] ?: throw ReThisException("Unsupported type for collection decoder: $argument [${currName()}]")
    addImport(decoder)

    addStatement(baseStatement, decoder.name)
}

internal fun CodeBlock.Builder.writeMapDecoder() {
    val currentCmd = context.currentCommand
    val baseArg = currentCmd.arguments.last().toTypeName()
    val argument = baseArg.copy(false)

    val decoder = mapDecoders[argument] ?: throw ReThisException("Unsupported type for map decoder: $argument [${currName()}]")
    addImport(decoder)

    val baseStatement = if (baseArg.isNullable) DECODE_STRING_N else DECODE_STRING
    addStatement(baseStatement, decoder.name)
}
