package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import eu.vendeli.rethis.api.processor.type.CommandArgument
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal val charsetClassName = ClassName("io.ktor.utils.io.charsets", "Charset")

internal val decodersMap: Map<RespCode, Pair<String?, String>> = mapOf(
    RespCode.SIMPLE_STRING to ("SimpleStringDecoder" to "SimpleStringDecoder.decode(input, charset)"),
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

internal fun KSClassDeclaration.isDataObject() = classKind == ClassKind.OBJECT && modifiers.contains(Modifier.DATA)
internal fun KSClassDeclaration.isSealed() = classKind == ClassKind.CLASS && modifiers.contains(Modifier.SEALED)
internal fun KSClassDeclaration.isEnum() = classKind == ClassKind.ENUM_CLASS

internal fun List<CommandArgument>.flattenArguments(): List<CommandArgument> = flatMap { argument ->
    listOf(argument) + argument.arguments.flattenArguments()
}
