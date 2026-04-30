package eu.vendeli.rethis.api.processor.core

import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.context.CodecFileSpec
import eu.vendeli.rethis.api.processor.context.CodecObjectTypeSpec
import eu.vendeli.rethis.api.processor.context.CurrentCommand
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.RCommandData
import eu.vendeli.rethis.api.processor.utils.*

internal object RedisProcessor {
    fun process(cmd: RCommandData) {
        val currentCmd = context[CurrentCommand] ?: run {
            cmd.reportError("No current command found in context")
            return
        }

        validate(currentCmd)
        addProcessedResponses(cmd.name, currentCmd.command.responseTypes)

        val specSigArguments = currentCmd.encodeFunction.parameters.associate { param ->
            param.name!!.asString() to Pair(
                param.type.resolve().toTypeName(),
                listOfNotNull(
                    if (param.isVararg) KModifier.VARARG else null,
                ),
            )
        }

//        if (context.validation.getErrors().isNotEmpty()) { // todo return validation
//            context.logger.error("Validation failed: ${context.validation.getErrors()}")
//            return
//        }
        val codecName = "${currentCmd.klass.simpleName.asString()}Codec"
        val cmdPackagePart = "." + currentCmd.klass.packageName.asString().substringAfterLast(".")

        val fileSpec = FileSpec.builder(context.meta.codecsPackage + cmdPackagePart, codecName)
            .indent(" ".repeat(4))
        val typeSpec = TypeSpec.objectBuilder(codecName)
        val rTypes = context.currentCommand.command.responseTypes.toSet()

        context += CodecFileSpec(fileSpec)
        context += CodecObjectTypeSpec(typeSpec)

        addEncoderCode()
        context.typeSpec.addDecodeFunction(
            rTypes,
            context.currentCommand.specType,
        )

        addMetaParameters()
        context.fileSpec.addType(context.typeSpec.build())

        context.curImports.forEach {
            val import = it.split(".")
            val name = import.last()
            fileSpec.addImport(import.dropLast(1).joinToString("."), name)
        }

        val commandFileSpec = context.commandFileSpec.getFor(currentCmd.command.name)

        commandFileSpec.addCommandFunctions(
            codecName = codecName,
            parameters = specSigArguments,
            type = currentCmd.type,
            cmdPackagePart = cmdPackagePart,
            responseTypes = rTypes,
        )

        val spec = context.fileSpec.build()
        runCatching {
            context.meta.codeGenerator.createNewFile(
                Dependencies(false, currentCmd.klass.containingFile!!),
                spec.packageName,
                spec.name,
            ).bufferedWriter().use { spec.writeTo(it) }
        }.onFailure { it.printStackTrace() }
    }

    private fun validate(command: CurrentCommand) {
        val isJson = command.command.name.startsWith("JSON.")
        val isSentinel = command.command.name.startsWith("SENTINEL")

        if (!isSentinel && !isJson) validateOperation(command)
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

    private fun validateBlocking(cCmd: CurrentCommand) {
        val expectedStatus = context.currentRSpec.commandFlags?.find { it.equals("blocking", true) } != null

        if (cCmd.command.isBlocking != expectedStatus) cCmd.reportError(
            "Blocking status mismatch: declared '${cCmd.command.isBlocking}' but spec flags imply '$expectedStatus'",
        )
    }

    private fun addMetaParameters() {
        val staticParts = buildStaticCommandParts(
            *context.currentCommand.command.name.split(' ').toTypedArray(),
            parameters = context.currentCommand.encodeFunction.parameters,
        )

        context.typeSpec
            .addProperty(
                PropertySpec.builder("BLOCKING_STATUS", BOOLEAN, KModifier.PRIVATE, KModifier.CONST)
                    .initializer("%L", context.currentCommand.command.isBlocking)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("COMMAND_HEADER", BYTE_ARRAY, KModifier.PRIVATE)
                    .initializer(buildStaticHeaderInitializer(staticParts))
                    .build(),
            )
    }
}
