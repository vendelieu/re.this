package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.symbol.KSAnnotated
import eu.vendeli.rethis.api.processor.utils.inferEnumValue
import eu.vendeli.rethis.api.processor.utils.parseResponseTypes
import eu.vendeli.rethis.api.processor.utils.safeCast
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

data class RCommandData(
    val name: String,
    val operation: RedisOperation = RedisOperation.READ,
    val responseTypes: List<RespCode>,
    val isBlocking: Boolean = false,
)

fun KSAnnotated.getCommandData(): RCommandData = annotations.filter {
    it.shortName.getShortName() == RedisCommand::class.simpleName && it.annotationType.resolve().declaration
        .qualifiedName?.asString() == RedisCommand::class.qualifiedName
}.firstOrNull()?.let { a ->
    val name = a.arguments.firstOrNull { it.name?.getShortName() == RedisCommand::name.name }?.value?.toString()
        ?: throw IllegalStateException("No 'name' argument found in @RedisCommand annotation on $this")
    val operation = a.arguments.firstOrNull {
        it.name?.getShortName() == RedisCommand::operation.name
    }?.value?.let { RedisOperation.valueOf(it.inferEnumValue()) }
        ?: throw IllegalStateException("No 'operation' argument found in @RedisCommand annotation on $this")
    val responseTypes = a.arguments.parseResponseTypes()
    val isBlocking = a.arguments.firstOrNull {
        it.name?.getShortName() == RedisCommand::isBlocking.name
    }?.value.safeCast<Boolean>()

    RCommandData(
        name = name,
        operation = operation,
        responseTypes = responseTypes.orEmpty(),
        isBlocking = isBlocking ?: false,
    )
} ?: throw IllegalStateException("No @RedisCommand annotation found on $this. Available annotations: ${annotations.map { it.shortName.getShortName() }}")
