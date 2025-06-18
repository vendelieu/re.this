package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import eu.vendeli.rethis.api.processor.types.CommandArgument
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal val charsetClassName = ClassName("io.ktor.utils.io.charsets", "Charset")
internal val commandRequestClassName = CommandRequest::class.asClassName()

internal val decodersMap: Map<RespCode, Pair<String?, String>> = mapOf(
    RespCode.SIMPLE_STRING to ("SimpleStringDecoder" to "SimpleStringDecoder.decode(input, charset, TYPE_INFO)"),
    RespCode.VERBATIM_STRING to ("VerbatimStringDecoder" to "VerbatimStringDecoder.decode(input, charset, TYPE_INFO)"),

    RespCode.BULK to ("BulkStringDecoder" to "BulkStringDecoder.decode(input, charset, TYPE_INFO)"),

    RespCode.INTEGER to ("IntegerDecoder" to "IntegerDecoder.decode(input, charset, TYPE_INFO)"),
    RespCode.DOUBLE to ("DoubleDecoder" to "DoubleDecoder.decode(input, charset, TYPE_INFO)"),
    RespCode.BOOLEAN to ("BooleanDecoder" to "BooleanDecoder.decode(input, charset, TYPE_INFO)"),
    RespCode.BIG_NUMBER to ("BigDecimalDecoder" to "BigDecimalDecoder.decode(input, charset, TYPE_INFO)"),

    RespCode.SET to ("SetDecoder" to "SetDecoder.decode<%s>(input, charset, TYPE_INFO)"),
    RespCode.ARRAY to ("ArrayDecoder" to "ArrayDecoder.decode<%s>(input, charset, TYPE_INFO)"),

    RespCode.MAP to ("MapDecoder" to "MapDecoder.decode<%s, %s>(input, charset, TYPE_INFO)"),

    RespCode.SIMPLE_ERROR to ("SimpleErrorDecoder" to "SimpleErrorDecoder.decode(input, charset, TYPE_INFO)"),
    RespCode.BULK_ERROR to ("BulkErrorDecoder" to "BulkErrorDecoder.decode(input, charset, TYPE_INFO)"),

    RespCode.NULL to (null to "null"),
)

internal fun String.specTypeNormalization() = when (this) {
    "key" -> "string"
    "integer" -> "long"
    "int" -> "long"
    "pattern" -> "string"
    "unix-time" -> "instant"
    else -> this
}

internal fun String.libTypeNormalization() = when (this) {
    "duration" -> "long"
    else -> this
}

internal fun KSClassDeclaration.tokenName() = getAnnotation<RedisOption.Token>()?.get("name") ?: effectiveName()

internal fun KSAnnotated.effectiveName() = getAnnotation<RedisOption.Name>()?.get("name") ?: when (this) {
    is KSDeclaration -> simpleName.asString()
    is KSValueParameter -> name!!.asString()
    else -> toString()
}

private val collectionTypes = setOf("kotlin.collections.List", "kotlin.collections.Set", "kotlin.Array")
internal fun KSType.isCollection(): Boolean {
    val typeName = declaration.qualifiedName?.asString() ?: return false
    return typeName in collectionTypes
}

internal fun KSDeclaration.isDataObject() =
    this is KSClassDeclaration && classKind == ClassKind.OBJECT && modifiers.contains(Modifier.DATA)

internal fun KSDeclaration.isSealed() =
    this is KSClassDeclaration && classKind == ClassKind.CLASS && modifiers.contains(Modifier.SEALED)

internal fun KSAnnotated.isStdType() =
    this is KSClassDeclaration && qualifiedName?.getQualifier()?.startsWith("kotlin") == true

internal fun KSDeclaration.isTimeType() = qualifiedName?.getQualifier()?.let {
    it.startsWith("kotlin.time") || it.startsWith("kotlinx.datetime")
} == true

internal fun KSDeclaration.isEnum() = this is KSClassDeclaration && classKind == ClassKind.ENUM_CLASS
internal fun KSDeclaration.isBool() = qualifiedName?.asString().let { it == "kotlin.Boolean" || it == "boolean" }

internal fun List<CommandArgument>.flattenArguments(): List<CommandArgument> = flatMap { argument ->
    listOf(argument) + argument.arguments.flattenArguments()
}
