package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.symbol.KSAnnotated
import eu.vendeli.rethis.api.processor.utils.inferEnumValue
import eu.vendeli.rethis.api.processor.utils.parseResponseTypes
import eu.vendeli.rethis.api.processor.utils.safeCast
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

data class RCommandData(
    val name: String,
    val operation: RedisOperation = RedisOperation.READ,
    val responseTypes: List<RespCode>,
    val isBlocking: Boolean = false,
)

fun KSAnnotated.getCommandData(): RCommandData = annotations.filter {
    it.shortName.getShortName() == RedisCommand::class.simpleName && it.annotationType.resolve().declaration
        .qualifiedName?.asString() == RedisCommand::class.qualifiedName
}.first().let { a ->
    val name = a.arguments.first { it.name?.getShortName() == RedisCommand::name.name }.value.toString()
    val operation = a.arguments.first {
        it.name?.getShortName() == RedisCommand::operation.name
    }.value?.let { RedisOperation.valueOf(it.inferEnumValue()) }!!
    val responseTypes = a.arguments.parseResponseTypes()
    val isBlocking = a.arguments.first {
        it.name?.getShortName() == RedisCommand::isBlocking.name
    }.value.safeCast<Boolean>()

    RCommandData(
        name = name,
        operation = operation,
        responseTypes = responseTypes ?: emptyList(),
        isBlocking = isBlocking ?: false,
    )
}
