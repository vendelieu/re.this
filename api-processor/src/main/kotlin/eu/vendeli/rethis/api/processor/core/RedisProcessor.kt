package eu.vendeli.rethis.api.processor.core

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.context.CodecFileSpec
import eu.vendeli.rethis.api.processor.context.CodecObjectTypeSpec
import eu.vendeli.rethis.api.processor.context.CurrentCommand
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.RCommandData
import eu.vendeli.rethis.api.processor.utils.*
import io.ktor.util.reflect.TypeInfo
import kotlinx.io.Buffer
import java.io.File

/*
 todo describe rules on how spec working
  for example `vararg param` -> param.forEach { writeArg(it) } | `param: List<>` -> writeListArg(param)
  consider all cases (when there's two param that needed to be written as forEach, vararg can be only one)
  --
  placing token on struct / parameter
  bool tokens
  --
  rewrite validation logic
 */
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

//        if (context.validation.getErrors().isNotEmpty()) { // todo return validation
//            context.logger.error("Validation failed: ${context.validation.getErrors()}")
//            return
//        }
        val codecName = "${currentCmd.klass.simpleName.asString()}Codec"
        val cmdPackagePart = "." + currentCmd.klass.packageName.asString().substringAfterLast(".")

        val fileSpec = FileSpec.builder(context.meta.codecsPackage + cmdPackagePart, codecName)
            .indent(" ".repeat(4))
        val typeSpec = TypeSpec.objectBuilder(codecName)

        context += CodecFileSpec(fileSpec)
        context += CodecObjectTypeSpec(typeSpec)

        addEncoderCode()
        context.typeSpec.addDecodeFunction(
            context.currentCommand.command.responseTypes.toList(),
            context.currentCommand.specType,
        )

        addMetaParameters()
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
            writeTo(File(context.meta.clientDir)) // todo return
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
                PropertySpec.builder("TYPE_INFO", TypeInfo::class, KModifier.PRIVATE)
                    .initializer("typeInfo<%T>()", context.currentCommand.type)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("BLOCKING_STATUS", BOOLEAN, KModifier.PRIVATE, KModifier.CONST)
                    .initializer("%L", context.currentCommand.command.isBlocking)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("COMMAND_HEADER", Buffer::class, KModifier.PRIVATE)
                    .initializer(buildStaticHeaderInitializer(staticParts))
                    .build(),
            )
    }
}
