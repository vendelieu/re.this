package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import eu.vendeli.rethis.api.processor.utils.safeCast
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand

internal data class ValidationContext(
    val func: KSFunctionDeclaration,
    val fullSpec: RedisCommandFullSpec,
    private val errors: MutableList<String>,
    val currentCmd: String,
    val logger: KSPLogger,
) {
    val curSpec = fullSpec.commands[currentCmd]!!
    val annotation = func.parent?.safeCast<KSAnnotated>()!!.annotations.first {
        it.shortName.asString() == RedisCommand::class.simpleName
    }
    val specTree = SpecTreeBuilder(curSpec.arguments.orEmpty()).build()
    val paramTree = LibSpecTreeBuilder.build(func)

    fun reportError(msg: String) {
        errors += msg
    }
}
