package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal fun List<KSValueArgument>.parseResponseTypes(): List<RespCode>? = firstOrNull {
    it.name?.asString() == "responseTypes"
}?.value?.safeCast<List<*>>()?.map {
    RespCode.valueOf(it.inferEnumValue())
}

internal fun Any?.inferEnumValue(): String = when (this) {
    is KSType -> this.declaration.toString()
    is KSClassDeclaration -> this.simpleName.getShortName()
    else -> throw IllegalStateException("Unknown type $this")
}
