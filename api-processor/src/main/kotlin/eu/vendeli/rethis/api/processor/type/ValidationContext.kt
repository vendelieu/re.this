package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import eu.vendeli.rethis.api.processor.utils.NameNormalizer

internal data class ValidationContext(
    val func: KSFunctionDeclaration,
    val specTree: List<SpecNode>,
    val fullSpec: RedisCommandFullSpec,
    val errors: MutableList<String>,
    val processed: MutableList<String>,
) {
    fun findParam(name: String): KSValueParameter? {
        val norm = NameNormalizer.normalizeParam(name)
        return func.parameters.firstOrNull { it.name?.asString() == norm }
    }

    fun markProcessed(name: String) {
//        processed += NameNormalizer.normalizeParam(name)
        processed += name
    }

    fun reportError(msg: String) {
        errors += msg
    }
}
