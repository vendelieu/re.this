package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.EnrichedNode
import eu.vendeli.rethis.api.processor.types.EnrichedTreeAttr
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
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

internal fun KSAnnotated.hasCustomEncoder(): Boolean {
    val customDecoder = getAnnotation<RedisMeta.CustomCodec>()?.get("encoder")
    return customDecoder != null &&
        customDecoder != Unit::class.simpleName &&
        customDecoder != Any::class.simpleName
}

internal fun KSAnnotated.hasCustomDecoder(): Boolean {
    val customDecoder = getAnnotation<RedisMeta.CustomCodec>()?.get("decoder")
    return customDecoder != null &&
        customDecoder != Nothing::class.simpleName &&
        !customDecoder.startsWith(ResponseDecoder::class.simpleName!!)
}

@OptIn(KspExperimental::class)
internal fun KSAnnotated.getCustom(): RedisMeta.CustomCodec? =
    getAnnotationsByType(RedisMeta.CustomCodec::class).firstOrNull()

internal fun KSType.getTimeUnit(): String = annotations.firstOrNull {
    it.shortName.asString() == RedisMeta.OutgoingTimeUnit::class.simpleName
}?.arguments?.firstOrNull()?.value?.toString().let {
    if (it == "SECONDS") "SECONDS" else "MILLISECONDS"
}

internal fun KSType.collectionAwareType(): KSType =
    if (isCollection()) arguments.first().type!!.resolve()
    else this

@OptIn(KspExperimental::class)
internal fun KSAnnotated.saveTokens(node: EnrichedNode) {
    getAnnotationsByType(RedisOption.Token::class).forEach { t ->
        val multipleToken = context.currentRSpec.allArguments.find { it.token == t.name }?.multipleToken
        node.attr.add(
            EnrichedTreeAttr.Token(
                t.name,
                multipleToken ?: false,
            ),
        )
    }
}

internal val KSType.name: String
    get() {
        val pkgName = declaration.packageName.asString()

        return declaration.qualifiedName!!.asString().removePrefix("${pkgName}.")
            .split('.').joinToString(".")
    }

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.safeCast(): R? = this as? R
