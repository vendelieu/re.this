package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import eu.vendeli.rethis.api.processor.utils.NameNormalizer

internal data class ValidationContext(
    val func: KSFunctionDeclaration,
    val specTree: List<SpecNode>,
    val fullSpec: RedisCommandFullSpec,
    val errors: MutableList<String>,
    val currentCmd: String,
) {
    val paramTree = SpecTreeBuilder.build(func)

    fun findParam(name: String): LibSpecTree.ParameterNode? {
        val normalizedName = NameNormalizer.normalizeParam(name)
        return paramTree.findParameterByName(normalizedName)
    }

    fun isTokenPresent(name: String): Boolean = paramTree.findTokenByName(name) != null

    fun markProcessed(name: String) {
    }

    fun reportError(msg: String) {
        errors += msg
    }
}
