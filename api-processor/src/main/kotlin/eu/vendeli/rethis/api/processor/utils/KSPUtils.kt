package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName
import eu.vendeli.rethis.api.processor.type.RedisCommandApiSpec
import eu.vendeli.rethis.api.processor.type.collectAllArguments
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun <reified T : Annotation> KSAnnotated.getAnnotation(): Map<String, String>? =
    annotations.firstOrNull {
        it.shortName.asString() == T::class.simpleName
    }?.let { annotation ->
        annotation.arguments.associate { (it.name?.getShortName() ?: "value") to it.value.toString() }
    }

internal inline fun <reified T : Annotation> KSAnnotated.hasAnnotation(): Boolean =
    annotations.any { it.shortName.asString() == T::class.simpleName }

@OptIn(ExperimentalContracts::class)
internal inline fun String?.ifNullOrEmpty(defaultValue: () -> String): String {
    contract {
        callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE)
    }
    return if (this == null || isEmpty()) defaultValue() else this
}

internal fun KSDeclaration.isStdType() = qualifiedName?.getQualifier()?.startsWith("kotlin") == true

internal fun FileSpec.Builder.typeWrite(
    param: KSValueParameter, value: String,
) = param.type.resolve().toClassName().simpleName.toPascalCase().let {
    val extName = "write${it}Arg"
    addImport("eu.vendeli.rethis.utils", extName)

    "buffer.$extName($value, charset)"
}

internal fun KSAnnotated.hasCustomEncoder(): Boolean {
    val customDecoder = getAnnotation<RedisMeta.CustomCodec>()?.get("encoder")
    return customDecoder != null && customDecoder != Unit::class.simpleName
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.safeCast(): R? = this as? R

internal fun KSPLogger.report(
    cmd: Map.Entry<String, List<KSClassDeclaration>>,
    spec: RedisCommandApiSpec,
    errors: Map<KSClassDeclaration, List<String>>,
    processedEntries: Set<String>,
    responseValidationResult: Pair<Set<RespCode>, Set<RespCode>>,
) {
    // Unprocessed arguments
    val unprocessed = spec.collectAllArguments().map { it.name }.filterNot { it in processedEntries }

    // Error entries
    val hasErrors = errors.any { it.value.isNotEmpty() }

    // Response mismatches
    val (redundant, missing) = responseValidationResult
    val hasRespIssues = redundant.isNotEmpty() || missing.isNotEmpty()
    val isResponseValidationIgnore = cmd.value.all { it.parseIgnore().contains(ValidityCheck.RESPONSE) }

    if (!hasErrors && !hasRespIssues && unprocessed.isEmpty()) return

    // Compact report line
    val parts = mutableListOf<String>()

    // Errors
    if (hasErrors) errors.filterValues { it.isNotEmpty() }.forEach { (decl, errs) ->
        val prefix = decl.qualifiedName?.asString() ?: decl.simpleName.asString()
        parts += "- $prefix:"
        errs.forEach { parts += "  • $it" }
    }

    // Unprocessed
    if (unprocessed.isNotEmpty()) parts += "- Unprocessed: ${unprocessed.joinToString()}"

    // Response
    if (!isResponseValidationIgnore && hasRespIssues) {
        parts += "- Response types validation issues:"
        if (redundant.isNotEmpty()) parts += "  • Redundant: ${redundant.joinToString()}"
        if (missing.isNotEmpty()) parts += "  • Missing: ${missing.joinToString()}"
    }

    if (parts.isEmpty()) return

    // Command
    parts.add(0, "${cmd.key}:")

    // Log each concise report on its own line
    val report = parts.chunked(1).joinToString("\n") { it.first() }
    error(report)
}
