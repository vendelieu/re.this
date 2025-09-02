package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.asClassName
import eu.vendeli.rethis.api.processor.types.CommandArgument
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.aggregate.*
import eu.vendeli.rethis.shared.decoders.general.*
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode

internal val charsetClassName = ClassName("io.ktor.utils.io.charsets", "Charset")
internal val commandRequestClassName = CommandRequest::class.asClassName()
internal val RTYPE = RType::class.asClassName()

internal val plainDecoders = mapOf(
    RespCode.SIMPLE_STRING to SimpleStringDecoder::class.qualifiedName,
    RespCode.VERBATIM_STRING to VerbatimStringDecoder::class.qualifiedName,
    RespCode.BULK to BulkStringDecoder::class.qualifiedName,
    RespCode.INTEGER to IntegerDecoder::class.qualifiedName,
    RespCode.DOUBLE to DoubleDecoder::class.qualifiedName,
    RespCode.BOOLEAN to BooleanDecoder::class.qualifiedName,
    RespCode.BIG_NUMBER to BigDecimalDecoder::class.qualifiedName,
    RespCode.SIMPLE_ERROR to SimpleErrorDecoder::class.qualifiedName,
    RespCode.BULK_ERROR to BulkErrorDecoder::class.qualifiedName,
    RespCode.NULL to "null",
)

internal val collectionDecoders = mapOf(
    RespCode.ARRAY to mapOf(
        LONG to ArrayLongDecoder::class.qualifiedName,
        RTYPE to ArrayRTypeDecoder::class.qualifiedName,
        STRING to ArrayStringDecoder::class.qualifiedName,
    ),
    RespCode.SET to mapOf(
        STRING to SetStringDecoder::class.qualifiedName,
    ),
)

internal val mapDecoders = mapOf(
    STRING to MapStringDecoder::class.qualifiedName,
    RTYPE to MapRTypeDecoder::class.qualifiedName,
)

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

internal fun KSDeclaration.isTimeType() = qualifiedName?.getQualifier()?.startsWith("kotlin.time") == true

internal fun KSDeclaration.isEnum() = this is KSClassDeclaration && classKind == ClassKind.ENUM_CLASS
internal fun KSDeclaration.isBool() = qualifiedName?.asString().let { it == "kotlin.Boolean" || it == "boolean" }

internal fun List<CommandArgument>.flattenArguments(): List<CommandArgument> = flatMap { argument ->
    listOf(argument) + argument.arguments.flattenArguments()
}
