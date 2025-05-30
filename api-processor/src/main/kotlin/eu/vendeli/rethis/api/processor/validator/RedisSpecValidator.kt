package eu.vendeli.rethis.api.processor.validator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.processor.types.RedisCommandFullSpec
import eu.vendeli.rethis.api.processor.types.ValidationContext
import eu.vendeli.rethis.api.processor.types.collectAllChildren
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal class RedisSpecValidator(
    private val logger: KSPLogger,
    private val fullSpec: RedisCommandFullSpec,
) {
    @OptIn(KspExperimental::class)
    fun initProcessing(cmd: Map.Entry<String, List<KSClassDeclaration>>) {
        if (cmd.key.startsWith("SENTINEL")) return // skip check for sentinel commands since there's no spec for them

        if (cmd.value.all { it.hasCustomEncoder() }) return

        val spec = fullSpec.commands[cmd.key]
        if (spec == null) {
            logger.error("No spec found for command `${cmd.key}`")
            return
        }
        val errors = mutableMapOf<KSClassDeclaration, MutableList<String>>()
        val processedResponses = mutableSetOf<RespCode>()
        val processedEntries = mutableSetOf<String>()

        cmd.value.forEach {
            if (it.hasCustomEncoder()) return@forEach
            val cmdErrorContainer = errors.getOrPut(it) { mutableListOf() }
            processedEntries += processCommand(cmd.key, it, cmdErrorContainer, processedResponses, cmd.value.size > 1)
        }
        val responseValidationResult = validateResponseTypes(cmd.key, processedResponses)

        logger.report(cmd, spec, errors, processedEntries, responseValidationResult)
    }

    private fun processCommand(
        command: String,
        c: KSClassDeclaration,
        errors: MutableList<String>,
        processedResponses: MutableSet<RespCode>,
        isMultiSpec: Boolean,
    ): Set<String> {
        val encodeFun = c.declarations.filterIsInstance<KSFunctionDeclaration>().firstOrNull {
            it.simpleName.asString() == "encode"
        }
        if (encodeFun == null) {
            errors += "No encode function found for ${c.qualifiedName}"
            return emptySet()
        }
        val ctx = ValidationContext(command, encodeFun, fullSpec, errors, logger, isMultiSpec)

        validateOperation(ctx)

        val declaredRTypes = ctx.annotation.arguments.parseResponseTypes()
        processedResponses += declaredRTypes.orEmpty()

        validateExtensions(ctx)
        validateBlockingStatus(ctx)

        validateKey(ctx)
        validateMeta(ctx)

        ctx.specTree.forEach { it.accept(SpecTreeValidator, ctx) }

        return ctx.specTree.flatMap {
            listOf(it) + it.collectAllChildren()
        }.filter { it.processed }.map { it.name }.toSet()
    }

    private fun validateKey(ctx: ValidationContext) = with(ctx) {
        val keys = curSpec.arguments?.filter { it.keySpecIndex != null && it.type == "key" }.orEmpty()
        if (keys.isEmpty()) return

        // validate CommandRequest key
        val commandRequestType = func.returnType?.resolve()?.arguments?.firstOrNull()?.type?.resolve()
        if (commandRequestType == null) {
            reportError("No return type found for encode function")
            return@with
        }

        val isKeyTypeNameList = commandRequestType.starProjection().toTypeName() == LIST.parameterizedBy(STAR)
        if ((keys.size > 1 || keys.any { it.multiple }) && !isKeyTypeNameList) {
            reportError("Multiple keys but command request type is not List<*>")
        }

        val desiredKeyType = keys.distinctBy {
            it.type
        }.takeIf { it.size == 1 }?.first()?.type?.specTypeNormalization() ?: "string"
        val actualKeyType = commandRequestType.let {
            if (isKeyTypeNameList) it.arguments.first().type?.resolve()?.toClassName()?.simpleName
            else it.toClassName().simpleName
        }?.lowercase()

        if (desiredKeyType != actualKeyType) {
            reportError("Key type mismatch: desired `$desiredKeyType` but actual `$actualKeyType`")
        }
    }

    private fun validateOperation(ctx: ValidationContext): Unit = with(ctx) {
        if (currentCmd.startsWith("JSON.")) return // JSON commands have no properties to check
        val declaredOp = annotation.arguments
            .firstOrNull { it.name?.asString() == "operation" }
            ?.value.inferEnumValue()

        curSpec.tryInferOperation()?.takeIf { declaredOp != it }?.also { expectedOp ->
            reportError("Operation mismatch: declared '$declaredOp' but spec flags imply '$expectedOp'")
        }
    }


    private fun validateExtensions(ctx: ValidationContext) = with(ctx) {
        val extensions = annotation.arguments.first { it.name?.asString() == "extensions" }
            .value?.safeCast<List<KSType>>()?.mapNotNull {
                it.declaration.qualifiedName?.asString()
            }?.toSet() ?: emptySet()

        func.parameters.forEach { p ->
            val extType = p.type.resolve().let {
                if (it.isCollection()) it.arguments.first().type!!.resolve()
                else it
            }
            if (!extType.declaration.isStdType() && extType.declaration.qualifiedName?.asString() !in extensions) {
                reportError("Parameter '${p.name?.asString()}' has type '$extType' which is not in extensions")
            }
        }

        extensions.forEach { ext ->
            val isPresent = func.parameters.any { it.type.resolve().declaration.qualifiedName?.asString() == ext }
            if (!isPresent) {
                reportError("Redundant extension '$ext' that is not present in parameters")
            }
        }
    }

    private fun validateBlockingStatus(ctx: ValidationContext) = with(ctx) {
        val blockingStatus = annotation.arguments.first { it.name?.asString() == "isBlocking" }
            .value?.safeCast<Boolean>() ?: false
        val expectedBlockingStatus = curSpec.commandFlags?.find { it.equals("blocking", true) } != null

        if (blockingStatus != expectedBlockingStatus) {
            reportError(
                "Blocking status mismatch: declared '$blockingStatus' but spec flags imply '$expectedBlockingStatus'",
            )
        }
    }

    private fun validateMeta(
        ctx: ValidationContext,
    ) = with(ctx) {
        func.parameters.filter { it.hasAnnotation<RedisMeta.WithSizeParam>() }.forEach { p ->
            val name = p.getAnnotation<RedisMeta.WithSizeParam>()?.get("name")
            specTree.find { it.name == name }?.processed = true
        }
    }

    private fun validateResponseTypes(
        cmd: String,
        processedResponses: MutableSet<RespCode>,
    ): Pair<Set<RespCode>, Set<RespCode>> {
        // skip json commands response validation since spec is incomplete
        if (cmd.startsWith("JSON.")) return emptySet<RespCode>() to emptySet()

        val expectedR2TypesDescr = fullSpec.resp2Responses[cmd]
        val expectedR3TypesDescr = fullSpec.resp3Responses[cmd]

        val expectedR2Types = expectedR2TypesDescr?.mapNotNull { it.inferResponseType() }
        val expectedR3Types = expectedR3TypesDescr?.mapNotNull { it.inferResponseType() }
        val expectedSummaryTypes = listOfNotNull(expectedR2Types, expectedR3Types).flatten().toSet()

        return processedResponses.minus(expectedSummaryTypes) to expectedSummaryTypes.minus(processedResponses)
    }
}
