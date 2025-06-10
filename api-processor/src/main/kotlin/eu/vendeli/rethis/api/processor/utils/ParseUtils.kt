package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ValidityCheck

internal fun List<KSValueArgument>.parseResponseTypes(): List<RespCode>? = firstOrNull {
    it.name?.asString() == RedisCommand::responseTypes.name
}?.value?.safeCast<List<*>>()?.map {
    RespCode.valueOf(it.inferEnumValue())
}

internal fun Any?.inferEnumValue(): String = when (this) {
    is KSType -> this.declaration.toString()
    is KSClassDeclaration -> this.simpleName.getShortName()
    else -> throw IllegalStateException("Unknown type $this")
}

internal fun KSAnnotated.parseIgnore(): Set<ValidityCheck> = annotations.firstOrNull {
    it.shortName.asString() == RedisMeta.IgnoreCheck::class.simpleName
}?.arguments?.firstOrNull {
    it.name?.asString() == "check"
}?.value?.safeCast<List<*>>()?.map {
    ValidityCheck.valueOf(it.inferEnumValue())
}?.toSet() ?: emptySet()
