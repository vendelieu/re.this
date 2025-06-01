package eu.vendeli.rethis.api.processor.processors

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.processor.utils.RedisSpecLoader.loadSpecs
import eu.vendeli.rethis.api.processor.validator.RedisSpecValidator
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.util.reflect.*
import kotlinx.io.Buffer
import java.io.File

class RedisCommandProcessor(
    private val logger: KSPLogger,
    options: Map<String, String>,
) : SymbolProcessor {
    private val clientDir = options["clientProjectDir"]!!
    private val codecsPackage = "eu.vendeli.rethis.codecs"
    private val commandPackage = "eu.vendeli.rethis.command"

    private val specs by lazy { loadSpecs() }
    private val validator = RedisSpecValidator(logger, specs)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ret = mutableListOf<KSAnnotated>()
        resolver.getSymbolsWithAnnotation(RedisCommand::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .also { c ->
                c.groupBy {
                    it.getAnnotation<RedisCommand>()?.get("name")!!
                }.entries.forEach {
                    validator.initProcessing(it)
                }
            }
            .forEach { cmd ->
                try {
                    processCommand(cmd)
                } catch (e: Exception) {
                    logger.error("Error processing ${cmd.qualifiedName?.asString()}: ${e.message}\n${e.stackTraceToString()}")
                    ret.add(cmd)
                }
            }

        return ret
    }

    private fun processCommand(cmd: KSClassDeclaration) {
        val annotation = cmd.getAnnotation<RedisCommand>()!!
        val coderName = "${cmd.simpleName.asString()}Codec"
        val cmdPackagePart = "." + cmd.packageName.asString().substringAfterLast(".")
        val commandName = annotation["name"].ifNullOrEmpty { cmd.simpleName.asString().uppercase() }
        val commandArguments = cmd.annotations.first {
            it.shortName.asString() == RedisCommand::class.simpleName
        }.arguments

        val encodeFunction = cmd.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .first { it.simpleName.asString() == "encode" }

        val parameters = encodeFunction.parameters
        val keyParam = parameters.find { it.hasAnnotation<RedisKey>() }
        val specType = cmd.superTypes.first().resolve().arguments.first() // RedisCommandSpec<T>
        val type = specType.toTypeName()
        val staticParts = buildStaticCommandParts(*commandName.split(' ').toTypedArray(), parameters = parameters)
        val responseTypes = commandArguments.parseResponseTypes()!!

        val specSigArguments = parameters.associate { param ->
            param.name!!.asString() to Pair(
                param.type.resolve().toTypeName(),
                listOfNotNull(
                    if (param.isVararg) KModifier.VARARG else null,
                ),
            )
        }

        val codecFileSpec = FileSpec.builder(codecsPackage + cmdPackagePart, coderName)
            .indent(" ".repeat(4))

        codecFileSpec.addType(
            TypeSpec.objectBuilder(coderName)
                .addProperty(
                    PropertySpec.builder("TYPE_INFO", TypeInfo::class, KModifier.PRIVATE)
                        .initializer("typeInfo<%T>()", type)
                        .build(),
                )
                .addProperty(
                    PropertySpec.builder("BLOCKING_STATUS", BOOLEAN, KModifier.PRIVATE, KModifier.CONST)
                        .initializer("%L", annotation["isBlocking"] ?: "false")
                        .build(),
                )
                .addProperty(
                    PropertySpec.builder("COMMAND_HEADER", Buffer::class, KModifier.PRIVATE)
                        .initializer(buildStaticHeaderInitializer(staticParts))
                        .build(),
                )
                .addEncodeFunction(codecFileSpec, annotation, specSigArguments, parameters, keyParam)
                .apply {
                    if (!cmd.hasCustomDecoder()) addDecodeFunction(responseTypes, specType)
                }
                .build(),
        ).apply {
            addImport("io.ktor.util.reflect", "TypeInfo", "typeInfo")
            addImport("io.ktor.utils.io.charsets", "Charsets")
            addImport("io.ktor.utils.io.core", "writeText")
            addImport("io.ktor.utils.io", "readByte")

            addImport("kotlinx.io", "readString", "writeString")
            addImport("eu.vendeli.rethis.api.spec.common.types", "RespCode", "UnexpectedResponseType")
            responseTypes.forEach {
                decodersMap[it]?.first?.let { decoder ->
                    addImport("eu.vendeli.rethis.api.spec.common.decoders", decoder)
                }
            }
            if (responseTypes.contains(RespCode.ARRAY) && responseTypes.contains(RespCode.MAP)) {
                addImport("eu.vendeli.rethis.api.spec.common.decoders", "ArrayMapDecoder")
            }
        }

        val commandFileSpec = FileSpec.builder(commandPackage + cmdPackagePart, annotation["name"]!!.toPascalCase())
            .indent(" ".repeat(4))

        commandFileSpec.addCommandFunctions(
            coderName, commandName, specSigArguments, type, cmdPackagePart, responseTypes,
        )

        codecFileSpec.build().runCatching { writeTo(File(clientDir)) }.onFailure { it.printStackTrace() }
        commandFileSpec.build().runCatching { writeTo(File(clientDir)) }.onFailure { it.printStackTrace() }
    }
}
