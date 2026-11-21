package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.EnrichedNode
import eu.vendeli.rethis.api.processor.types.EnrichedTreeAttr
import eu.vendeli.rethis.api.processor.types.WriteOp
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.decoders.ResponseDecoder

internal inline fun <reified T : Annotation> KSAnnotated.getAnnotation(): Map<String, String>? =
    annotations.firstOrNull {
        it.shortName.asString() == T::class.simpleName
    }?.let { annotation ->
        annotation.arguments.associate { (it.name?.getShortName() ?: "value") to it.value.toString() }
    }

internal inline fun <reified T : Annotation> KSAnnotated.hasAnnotation(): Boolean =
    annotations.any { it.shortName.asString() == T::class.simpleName }

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
        !isDefaultDecoderName(customDecoder)
}

/** The default decoder is `ResponseDecoder`, optionally followed by type arguments (`ResponseDecoder<...>`). */
internal fun isDefaultDecoderName(name: String): Boolean {
    val defaultName = ResponseDecoder::class.simpleName!!
    return name == defaultName || name.startsWith("$defaultName<")
}

@OptIn(KspExperimental::class)
internal fun KSAnnotated.getCustom(): RedisMeta.CustomCodec? =
    getAnnotationsByType(RedisMeta.CustomCodec::class).firstOrNull()

internal enum class OutgoingUnit { SECONDS, MILLISECONDS }

/** Maps an `OutgoingTimeUnit` argument (`TimeUnit.SECONDS` or bare `SECONDS`) to a supported unit, or null. */
internal fun resolveOutgoingUnit(raw: Any?): OutgoingUnit? {
    val name = when (raw) {
        null -> return null
        is KSType, is KSClassDeclaration -> raw.inferEnumValue()
        else -> raw.toString()
    }
    return OutgoingUnit.entries.firstOrNull { it.name == name.substringAfterLast('.') }
}

/** The unit declared by `@OutgoingTimeUnit` on this type, or null when absent or unsupported. */
internal fun KSType.getTimeUnit(): OutgoingUnit? = resolveOutgoingUnit(
    annotations.firstOrNull {
        it.shortName.asString() == RedisMeta.OutgoingTimeUnit::class.simpleName
    }?.arguments?.firstOrNull()?.value,
)

internal fun KSType.collectionAwareType(): KSType =
    if (isCollection()) arguments.first().type!!.resolve()
    else this

@OptIn(KspExperimental::class)
internal fun KSAnnotated.saveTokens(node: EnrichedNode) {
    getAnnotationsByType(RedisOption.Token::class).forEach { t ->
        val spec = context.currentRSpec.allNodes.find { specTokenMatches(it.arg.token, t.name) }
        val multipleToken = spec?.arg?.multipleToken

        if (spec != null) {
            node.attr.add(
                EnrichedTreeAttr.RelatedRSpec(spec)
            )
        }
        node.attr.add(
            EnrichedTreeAttr.Token(
                t.name,
                multipleToken ?: false,
            ),
        )
    }
}

internal fun List<Int>.isWithinBounds(bounds: List<Int>): Boolean {
    if (bounds.size < size - 1) return false
    repeat(size - 1) { idx ->
        if (get(idx) != bounds[idx]) return false
    }

    return true
}

internal fun List<WriteOp>.findWrappedCall(
    predicate: (WriteOp.WrappedCall) -> Boolean
): WriteOp? {
    fun recurse(op: WriteOp): WriteOp? {
        return when (op) {
            is WriteOp.WrappedCall -> when {
                predicate(op) -> op
                else -> op.inner.singleOrNull { recurse(it) != null }?.let { recurse(it) }
            }
            else -> null
        }
    }

    return singleOrNull { recurse(it) != null }
}


internal val KSType.name: String
    get() {
        val pkgName = declaration.packageName.asString()

        return declaration.qualifiedName!!.asString().removePrefix("${pkgName}.")
            .split('.').joinToString(".")
    }

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.safeCast(): R? = this as? R
