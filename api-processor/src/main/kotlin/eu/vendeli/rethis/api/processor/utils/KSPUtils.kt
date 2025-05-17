package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
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

internal fun FileSpec.Builder.typeWrite(param: KSValueParameter, value: String) =
    param.type.resolve().toClassName().simpleName.toPascalCase().let {
        val extName = "write${it}Arg"
        addImport("eu.vendeli.rethis.utils", extName)

        "buffer.$extName($value, charset)"
    }

internal fun KSType.toKSPName() = if (arguments.isEmpty()) toClassName() else toTypeName()

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <reified R> Any?.safeCast(): R? = this as? R
