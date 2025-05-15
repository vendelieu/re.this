package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.symbol.*
import eu.vendeli.rethis.api.processor.utils.getAnnotation
import eu.vendeli.rethis.api.processor.utils.hasAnnotation
import eu.vendeli.rethis.api.processor.utils.isDataObject
import eu.vendeli.rethis.api.processor.utils.tokenName
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class LibSpecNode {
    open val parent: LibSpecNode? = null
    open val children: MutableList<LibSpecNode> = mutableListOf()

    data class TokenNode(
        override val parent: LibSpecNode? = null,
        val name: String,
        override val children: MutableList<LibSpecNode> = mutableListOf(),
    ) : LibSpecNode()

    data class ParameterNode(
        override val parent: LibSpecNode? = null,
        val name: String,
        val symbol: KSValueParameter,
        override val children: MutableList<LibSpecNode> = mutableListOf(),
    ) : LibSpecNode()

    data class ContainerNode(
        override val parent: LibSpecNode? = null,
        val symbol: KSDeclaration,
        override val children: MutableList<LibSpecNode> = mutableListOf(),
    ) : LibSpecNode()
}

/**
 * Find a ParameterNode anywhere in the tree by its effective name.
 */
fun LibSpecNode.findParameterByName(name: String, root: LibSpecNode = this): LibSpecNode.ParameterNode? = when (root) {
    is LibSpecNode.ParameterNode -> if (root.name == name) root else null
    else -> root.children.asSequence()
        .mapNotNull { findParameterByName(name, it) }
        .firstOrNull()
}

/**
 * Find a TokenNode anywhere in the tree by its token name.
 */
fun LibSpecNode.findTokenByName(token: String, root: LibSpecNode = this): LibSpecNode.TokenNode? = when (root) {
    is LibSpecNode.TokenNode -> if (root.name == token) root else null
    else -> root.children.asSequence()
        .mapNotNull { findTokenByName(token, it) }
        .firstOrNull()
}


object SpecTreeBuilder {
    fun build(function: KSFunctionDeclaration): LibSpecNode.ContainerNode {
        val root = LibSpecNode.ContainerNode(parent = null, symbol = function)
        function.parameters.forEach { root.children += buildNode(it, root) }
        return root
    }

    private fun buildNode(
        param: KSValueParameter,
        parent: LibSpecNode,
    ): LibSpecNode {
        // 1. Determine effective name override
        val effectiveName = param.getAnnotation<RedisOption.Name>()?.get("name") ?: param.name!!.asString()

        // 2. Possibly wrap in parameter-level token
        var currentParent: LibSpecNode = parent
        param.getAnnotation<RedisOption.Token>()?.get("name")?.let { tokenName ->
            val tokNode = LibSpecNode.TokenNode(parent = currentParent, name = tokenName)
            currentParent.children += tokNode
            currentParent = tokNode
        }

        // 3. Inspect type
        val decl = param.type.resolve().declaration as? KSClassDeclaration

        currentParent = LibSpecNode.ParameterNode(currentParent, effectiveName, param)

        return when {
            // A. Sealed hierarchy container
            decl != null && decl.hasAnnotation<RedisOptionContainer>() -> {
                val container = LibSpecNode.ContainerNode(currentParent, decl)
                currentParent.children += container
                decl.declarations.filterIsInstance<KSClassDeclaration>().forEach { sub ->
                    // Enum or data object -> simple token
                    if (sub.classKind == ClassKind.ENUM_ENTRY || sub.isDataObject()) {
                        container.children += LibSpecNode.TokenNode(container, sub.tokenName())
                    } else {
                        // Treat subclass constructor parameters as fresh parameters
                        sub.primaryConstructor?.parameters?.forEach { p ->
                            container.children += buildNode(p, container)
                        }
                    }
                }
                container
            }
            // B. Token-decorated type (data class or object)
            decl != null && (decl.hasAnnotation<RedisOption.Token>() || decl.isDataObject()) -> {
                val tokName = decl.getAnnotation<RedisOption.Token>()?.get("name") ?: decl.simpleName.asString()
                val tokNode = LibSpecNode.TokenNode(currentParent, tokName)
                currentParent.children += tokNode
                // Recurse into its constructor (for data classes)
                decl.primaryConstructor?.parameters?.forEach { p ->
                    tokNode.children += buildNode(p, tokNode)
                }
                tokNode
            }
            // C. Data class or regular class without token
            decl != null && decl.classKind == ClassKind.CLASS
                && decl.qualifiedName?.asString()?.startsWith("kotlin") == false -> {
                // Flatten its params as if direct children
                val container = LibSpecNode.ContainerNode(currentParent, decl)
                currentParent.children += container
                decl.primaryConstructor?.parameters?.forEach { p ->
                    container.children += buildNode(p, container)
                }
                container
            }
            // D. Plain parameter
            else -> currentParent
        }
    }
}

