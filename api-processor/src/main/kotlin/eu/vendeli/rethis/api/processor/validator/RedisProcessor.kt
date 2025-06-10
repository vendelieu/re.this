package eu.vendeli.rethis.api.processor.validator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.context.CodecFileSpec
import eu.vendeli.rethis.api.processor.context.CodecMeta
import eu.vendeli.rethis.api.processor.context.CodecObjectTypeSpec
import eu.vendeli.rethis.api.processor.context.CurrentCommand
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.RCommandData
import eu.vendeli.rethis.api.processor.utils.*
import java.io.File

internal object RedisProcessor {
    fun process(cmd: RCommandData) {
        val currentCmd = context[CurrentCommand] ?: run {
            cmd.reportError("No current command found in context")
            return
        }
        if (cmd.name.startsWith("SENTINEL")) return

        validate(currentCmd)
        if (currentCmd.hasCustomEncoder) {
            addProcessedResponses(cmd.name, currentCmd.command.responseTypes)
            return
        }

        val specSigArguments = currentCmd.encodeFunction.parameters.associate { param ->
            param.name!!.asString() to Pair(
                param.type.resolve().toTypeName(),
                listOfNotNull(
                    if (param.isVararg) KModifier.VARARG else null,
                ),
            )
        }

        context.rSpecTree.forEach { it.accept(SpecTreeValidator) }
//        if (context.validation.getErrors().isNotEmpty()) {
//            context.logger.error("Validation failed: ${context.validation.getErrors()}")
//            return
//        }
        val codecName = "${currentCmd.klass.simpleName.asString()}Codec"

        val cmdPackagePart = "." + currentCmd.klass.packageName.asString().substringAfterLast(".")
        context += CodecMeta(codecPackage = cmdPackagePart, codecName = codecName)

        val fileSpec = FileSpec.builder(context.meta.codecsPackage + cmdPackagePart, codecName)
            .indent(" ".repeat(4))
        val typeSpec = TypeSpec.objectBuilder(codecName)

        context += CodecFileSpec(fileSpec)
        context += CodecObjectTypeSpec(typeSpec)

        addMetaParameters()
        addEncoderCode()
        context.typeSpec.addDecodeFunction(
            context.currentCommand.command.responseTypes.toList(),
            context.currentCommand.specType,
        )

        // 8) Finally, add the object to file and return
        context.fileSpec.addType(context.typeSpec.build())

        context.curImports.forEach {
            val import = it.split(".")
            val name = import.last()
            fileSpec.addImport(import.dropLast(1).joinToString("."), name)
        }

        val commandFileSpec = FileSpec.builder(
            context.meta.commandPackage + cmdPackagePart, currentCmd.command.name.toPascalCase(),
        ).indent(" ".repeat(4))

        val responseTypes = currentCmd.command.responseTypes.toList()

        commandFileSpec.addCommandFunctions(
            codecName = codecName,
            commandName = currentCmd.command.name,
            parameters = specSigArguments,
            type = currentCmd.type,
            cmdPackagePart = cmdPackagePart,
            responseTypes = responseTypes,
        )

        context.fileSpec.build().runCatching {
            writeTo(File(context.meta.clientDir))
        }.onFailure { it.printStackTrace() }
        commandFileSpec.build().runCatching {
//            writeTo(File(context.meta.clientDir)) todo return
        }.onFailure { it.printStackTrace() }
    }

    private fun validate(command: CurrentCommand) {
        val isJson = command.command.name.startsWith("JSON.")
        val isSentinel = command.command.name.startsWith("SENTINEL")

        if (!isSentinel && !isJson) validateOperation(command)
//        validateExtensions(command)
        if (!isSentinel) validateBlocking(command)
    }

    private fun validateOperation(cCmd: CurrentCommand) {
        // JSON commands have no properties to check
        context.currentRSpec.tryInferOperation()?.takeIf {
            cCmd.command.operation.name != it
        }?.also { expectedOp ->
            cCmd.reportError(
                "Operation mismatch: declared '${cCmd.command.operation}' but spec flags imply '$expectedOp'",
            )
        }
    }

    private fun validateExtensions(cCmd: CurrentCommand) {
        val extensions = context.currentCommand.command.extensions
        cCmd.encodeFunction.parameters.forEach { p ->
            val extType = p.type.collectionAwareType()
            val extTName = extType.toTypeName().copy(false)
            if (!extType.declaration.isStdType() && extTName !in extensions) {
                cCmd.reportError("Parameter '${p.name?.asString()}' has type '$extType' which is not in extensions")
            }
        }

        extensions.forEach { ext ->
            val isPresent = cCmd.encodeFunction.parameters.any {
                it.type.resolve().toTypeName().copy(false) == ext
            }
            if (!isPresent) cCmd.reportError("Redundant extension '$ext' that is not present in parameters")
        }
    }

    private fun validateBlocking(cCmd: CurrentCommand) {
        val expectedStatus = context.currentRSpec.commandFlags?.find { it.equals("blocking", true) } != null

        if (cCmd.command.isBlocking != expectedStatus) cCmd.reportError(
            "Blocking status mismatch: declared '${cCmd.command.isBlocking}' but spec flags imply '$expectedStatus'",
        )
    }
}
