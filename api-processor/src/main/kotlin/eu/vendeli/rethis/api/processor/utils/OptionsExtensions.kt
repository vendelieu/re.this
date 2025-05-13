package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import kotlinx.io.Buffer
import kotlin.sequences.forEach

internal fun FileSpec.Builder.generateOptionEncoders(
    optionContainer: KSClassDeclaration?,
) {
    optionContainer?.sealedSubclasses?.filter { c ->
        c.annotations.any {
            it.shortName.asString() != RedisOption::class.simpleName
        }
    }?.forEach { option ->
        val optionName = option.getAnnotation<RedisOption>()?.get("command") ?: option.simpleName.asString()
        when {
            option.isObject() -> generateObjectOptionEncoder(option, optionName)
            option.isDataClass() -> generateDataClassOptionEncoder(option, optionName)
            else -> null
        }?.let {
            addFunction(it)
        } ?: return@forEach
    }
}

private fun generateObjectOptionEncoder(
    option: KSClassDeclaration,
    optionName: String,
): FunSpec = FunSpec.builder("encode${option.simpleName.asString()}")
    .addParameter("charset", charsetClassName)
    .receiver(Buffer::class)
    .addStatement("writeStringArg(%S, charset)", optionName)
    .build()

private fun FileSpec.Builder.generateDataClassOptionEncoder(
    option: KSClassDeclaration,
    optionName: String,
): FunSpec = FunSpec.builder("encode${option.simpleName.asString()}")
    .receiver(Buffer::class)
    .apply {
        addParameter("charset", charsetClassName)
        option.primaryConstructor?.parameters?.forEach { param ->
            addParameter(param.name!!.asString(), param.type.toTypeName())
        }
    }
    .addStatement("writeStringArg(%S, charset)", optionName)
    .apply {
        option.primaryConstructor?.parameters?.forEach { param ->
            addStatement(typeWrite(param, param.name!!.asString()))
        }
    }
    .build()


private fun KSClassDeclaration.isDataClass(): Boolean =
    classKind == ClassKind.CLASS && modifiers.contains(Modifier.DATA)

private fun KSClassDeclaration.isObject(): Boolean = classKind == ClassKind.OBJECT

private val KSClassDeclaration.sealedSubclasses: Sequence<KSClassDeclaration>
    get() = getSealedSubclasses().mapNotNull {
        it.closestClassDeclaration()
    }
