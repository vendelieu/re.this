package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal data class ValidationContext(
    val func: KSFunctionDeclaration,
    val specTree: List<SpecNode>,
    val fullSpec: RedisCommandFullSpec,
    val errors: MutableList<String>,
    val currentCmd: String,
    val logger: KSPLogger,
) {
    val paramTree = LibSpecTreeBuilder.build(func)

    fun reportError(msg: String) {
        errors += msg
    }
}
