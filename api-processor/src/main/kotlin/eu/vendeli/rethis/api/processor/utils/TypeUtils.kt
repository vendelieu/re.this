package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal val charsetClassName = ClassName("io.ktor.utils.io.charsets", "Charset")

internal val decodersMap: Map<RespCode, Pair<String?, String>> = mapOf(
    RespCode.SIMPLE_STRING to ("SimpleStringDecoder" to "SimpleStringDecoder.decode(input, charset)"),
    RespCode.NULL to (null to "null"),
)

internal val stdTypes = mapOf(
    STRING to "kotlin.String",
    LONG to "kotlin.Long",
    DOUBLE to "kotlin.Double",
    BOOLEAN to "kotlin.Boolean",
    Duration::class.asClassName() to "kotlin.time.Duration",
    Instant::class.asClassName() to "kotlinx.datetime.Instant",
)

internal fun KSClassDeclaration.tokenName() = getAnnotation<RedisOption.Token>()?.get("name") ?: simpleName.asString()

internal fun KSAnnotated.effectiveName() = getAnnotation<RedisOption.Name>()?.get("name") ?: when (this) {
    is KSDeclaration -> simpleName.asString()
    is KSValueParameter -> name!!.asString()
    else -> toString()
}

internal fun KSClassDeclaration.isDataObject() = classKind == ClassKind.OBJECT && modifiers.contains(Modifier.DATA)
internal fun KSClassDeclaration.isSealed() = classKind == ClassKind.CLASS && modifiers.contains(Modifier.SEALED)
internal fun KSClassDeclaration.isEnum() = classKind == ClassKind.ENUM_CLASS
