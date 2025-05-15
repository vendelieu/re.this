package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.type.RedisCommandApiSpec
import eu.vendeli.rethis.api.processor.type.RedisCommandFullSpec
import eu.vendeli.rethis.api.processor.type.ValidationContext
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal class RedisSpecValidator(
    private val logger: KSPLogger,
    private val fullSpec: RedisCommandFullSpec,
) {
    private val paramValidator = SpecTreeValidator()

    fun initProcessing(cmd: Map.Entry<String, List<KSClassDeclaration>>) {
        val spec = fullSpec.commands[cmd.key]
        val processedParams = mutableListOf<String>()
        if (spec == null) {
            logger.error("No spec found for command `${cmd.key}`")
            return
        }
        val errors = mutableMapOf<KSClassDeclaration, MutableList<String>>()
        val processedResponses = mutableSetOf<RespCode>()

        cmd.value.forEach {
            val cmdErrorContainer = errors.getOrPut(it) { mutableListOf() }
            processCommand(cmd.key to spec, it, cmdErrorContainer, processedParams, processedResponses)
        }

        val mainValidationReport = errors.entries.filter { it.value.isNotEmpty() }.joinToString("\n") { e ->
            "${e.key.qualifiedName?.asString()}\n${e.value.joinToString("\n") { "- $it" }}"
        }.takeIf { it.isNotBlank() }
        val notProcessedParams = spec.arguments?.filterNot { it.specName in processedParams } ?: emptyList()
        val responseTypeValidationReport = validateResponseTypes(cmd.key, processedResponses).takeIf { it.isNotEmpty() }

        buildString {
            if (
                mainValidationReport != null || notProcessedParams.isNotEmpty() || responseTypeValidationReport != null
            ) {
                append(cmd.key)
                if (mainValidationReport == null) cmd.value.singleOrNull()?.also {
                    append(" - ${it.qualifiedName?.asString()}")
                }
                append("\n")
            }

            mainValidationReport?.also(::appendLine)
            if (
                mainValidationReport != null && (notProcessedParams.isNotEmpty() || responseTypeValidationReport != null)
            ) appendLine("------")

            if (notProcessedParams.isNotEmpty()) appendLine(
                "- Still unprocessed parameters: [${notProcessedParams.joinToString { it.normalizedName }}]",
            )
            responseTypeValidationReport?.let { appendLine("- Response types validation issues:\n$it") }
        }.takeIf { !it.isBlank() }?.let { logger.error(it) }
    }

    private fun processCommand(
        spec: Pair<String, RedisCommandApiSpec>,
        c: KSClassDeclaration,
        errors: MutableList<String>,
        processedParams: MutableList<String>,
        processedResponses: MutableSet<RespCode>,
    ) {
        val encodeFun = c.declarations.filterIsInstance<KSFunctionDeclaration>().firstOrNull {
            it.simpleName.asString() == "encode"
        }
        if (encodeFun == null) {
            errors += "No encode function found for ${c.qualifiedName}"
            return
        }
        val annotation = c.annotations.first { it.shortName.asString() == RedisCommand::class.simpleName }
//        validateOperation(spec, annotation, errors) // TODO turn on again

        val declaredRTypes = annotation.arguments.parseResponseTypes()
        processedResponses.addAll(declaredRTypes.orEmpty())

        validateExtensions(annotation, encodeFun, errors)
        validateBlockingStatus(annotation, spec.second, errors)

        validateKey(spec, encodeFun, errors)
        spec.second.arguments?.also {
            val tree = SpecTreeBuilder(it).build()
            val ctx = ValidationContext(encodeFun, tree, fullSpec, errors, processedParams)
            paramValidator.validateAll(ctx)
        }
        validateMeta(encodeFun, processedParams, errors)
    }

    private fun validateKey(
        spec: Pair<String, RedisCommandApiSpec>,
        f: KSFunctionDeclaration,
        errors: MutableList<String>,
    ) {
        val keys = spec.second.arguments?.filter { it.keySpecIndex != null && it.type == "key" } ?: emptyList()
        if (keys.isEmpty()) return

        // validate CommandRequest key
        val commandRequestType = f.returnType?.resolve()?.arguments?.firstOrNull()?.type?.resolve()
        if (commandRequestType == null) {
            errors += "No return type found for encode function"
        } else {
            val isKeyTypeNameList = commandRequestType.starProjection().toTypeName() == LIST.parameterizedBy(STAR)
            if (keys.size > 1 && !isKeyTypeNameList) {
                errors += "Multiple keys but command request type is not List<*>"
            }

            val desiredKeyType = keys.distinctBy {
                it.type
            }.takeIf { it.size == 1 }?.first()?.type?.specTypeNormalization() ?: "string"
            val actualKeyType = commandRequestType.let {
                if (isKeyTypeNameList) it.arguments.first().type?.resolve()?.toClassName()?.simpleName
                else it.toClassName().simpleName
            }?.lowercase()

            if (desiredKeyType != actualKeyType) {
                errors += "Key type mismatch: desired `$desiredKeyType` but actual `$actualKeyType`"
            }
        }


        keys.forEach { k ->
            val keyParam = f.parameters.find { it.name?.asString() == k.name }
            if (keyParam == null) {
                errors += "Not found key-parameter `${k.name}`"
                return@forEach
            }

            if (!keyParam.hasAnnotation<RedisKey>()) {
                errors += "Parameter `${keyParam.name?.asString()}` is not annotated with @RedisKey"
            }

            if ((keyParam.isVararg || keyParam.type.resolve().isCollection()) && !k.multiple) {
                errors += "Parameter `${keyParam.name?.asString()}` is vararg/list but spec.multiple=false"
            }
        }
    }


    private val collectionTypes = setOf("kotlin.collections.List", "kotlin.collections.Set", "kotlin.Array")
    private fun KSType.isCollection(): Boolean {
        val typeName = declaration.qualifiedName?.asString() ?: return false
        return typeName in collectionTypes
    }

    private fun String.specTypeNormalization() = when (this) {
        "key" -> "string"
        "integer" -> "long"
        "int" -> "long"
        "pattern" -> "string"
        "unix-time" -> "instant"
        else -> this
    }

    private fun String.libTypeNormalization() = when (this) {
        "duration" -> "long"
        else -> this
    }


    private fun inferOperation(flags: List<String>): String =
        if (flags.any { it.equals("readonly", true) } || flags.none {
                it.equals("write", true) || it.equals(
                    "admin",
                    true,
                )
            })
            "READ"
        else
            "WRITE"

    private fun validateOperation(
        spec: Pair<String, RedisCommandApiSpec>,
        a: KSAnnotation,
        errors: MutableList<String>,
    ) {
        if (spec.first.startsWith("JSON.")) return // JSON commands have no properties to check
        val declaredOp = a.arguments
            .firstOrNull { it.name?.asString() == "operation" }
            ?.value.inferEnumValue()

        if (spec.second.commandFlags == null) {
            if (declaredOp != "READ") errors += "Operation mismatch: declared '$declaredOp' but should be READ (spec flags are empty)"
            return
        }
        val expectedOp = inferOperation(spec.second.commandFlags!!)
        if (declaredOp != expectedOp) {
            errors += "Operation mismatch: declared '$declaredOp' but spec flags imply '$expectedOp'"
        }
    }


    private fun validateExtensions(
        a: KSAnnotation,
        f: KSFunctionDeclaration,
        errors: MutableList<String>,
    ) {
        val extensions = a.arguments.first { it.name?.asString() == "extensions" }
            .value?.safeCast<List<KSType>>()?.mapNotNull {
                it.declaration.qualifiedName?.asString()
            }?.toSet() ?: emptySet()

        f.parameters.forEach { p ->
            val extType = p.type.resolve().declaration.qualifiedName?.asString()!!
            if (extType !in stdTypes.values && extType !in extensions) {
                errors += "Parameter '${p.name?.asString()}' has type '$extType' which is not in extensions"
            }
        }

        extensions.forEach { ext ->
            val isPresent = f.parameters.any { it.type.resolve().declaration.qualifiedName?.asString() == ext }
            if (!isPresent) {
                errors += "Redundant extension '$ext' that is not present in parameters"
            }
        }
    }

    private fun String.inferResponseType(): RespCode? = when {
        checkMatch("simple string reply") -> RespCode.SIMPLE_STRING
        checkMatch("integer reply") -> RespCode.INTEGER
        checkMatch("boolean reply") -> RespCode.BOOLEAN
        checkMatch("double reply") -> RespCode.DOUBLE
        checkMatch("verbatim string reply") -> RespCode.VERBATIM_STRING
        checkMatch("big number reply") -> RespCode.BIG_NUMBER
        checkMatch("bulk string reply") -> RespCode.BULK
        checkMatch("simple error reply") -> RespCode.SIMPLE_ERROR

        contains("array reply", true) -> RespCode.ARRAY
        contains("set reply", true) -> RespCode.SET
        contains("map reply", true) -> RespCode.MAP
        contains("null reply", true) || contains("nil reply", true) -> RespCode.NULL
        else -> null
    }

    private fun validateResponseTypes(
        cmd: String,
        processedResponses: MutableSet<RespCode>,
    ): StringBuilder {
        val responseValidationReport = StringBuilder()
        val expectedR2TypesDescr = fullSpec.resp2Responses[cmd]
        val expectedR3TypesDescr = fullSpec.resp3Responses[cmd]

        if (cmd.startsWith("JSON.")) return responseValidationReport
        // skip json commands response validation since spec is incomplete

//        if (expectedR2TypesDescr == null && expectedR3TypesDescr == null) {
//            errors += "No responseTypes found in spec"
//            return
//        } todo check response type invalid specs

        val expectedR2Types = expectedR2TypesDescr?.mapNotNull { it.inferResponseType() }
        val expectedR3Types = expectedR3TypesDescr?.mapNotNull { it.inferResponseType() }
        val expectedSummaryTypes = listOfNotNull(expectedR2Types, expectedR3Types).flatten().toSet()

        // todo get array<?> from spec

        processedResponses.minus(expectedSummaryTypes).takeIf { it.isNotEmpty() }?.let {
            responseValidationReport.appendLine("   - Have redundant response types: ${it.joinToString(", ")}")
        }

        expectedSummaryTypes.minus(processedResponses).takeIf { it.isNotEmpty() }?.let {
            responseValidationReport.appendLine("   - Absent response types: ${it.joinToString(", ")}")
        }

        return responseValidationReport
    }

    private fun validateBlockingStatus(
        a: KSAnnotation,
        spec: RedisCommandApiSpec,
        errors: MutableList<String>,
    ) {
        val blockingStatus = a.arguments.first { it.name?.asString() == "isBlocking" }
            .value?.safeCast<Boolean>() ?: false
        val expectedBlockingStatus = spec.commandFlags?.find { it.equals("blocking", true) } != null

        if (blockingStatus != expectedBlockingStatus) {
            errors += "Blocking status mismatch: declared '$blockingStatus' but spec flags imply '$expectedBlockingStatus'"
        }
    }

    private fun validateMeta(
        f: KSFunctionDeclaration,
        processedParams: MutableList<String>,
        errors: MutableList<String>,
    ) {
        f.parameters.forEach { p ->
            p.getAnnotation<RedisMeta.WithSizeParam>()?.get("name")?.let { processedParams.add(it) }
        }
    }
}
