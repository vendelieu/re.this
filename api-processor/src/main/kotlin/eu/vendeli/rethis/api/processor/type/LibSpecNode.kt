package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.symbol.*
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

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
        function.parameters.forEach { handleValueParam(it, root) }
        return root
    }

    // 1. Handle a value parameter
    private fun handleValueParam(
        param: KSValueParameter,
        parent: LibSpecNode,
    ) {
        // Determine name override or default
        val name = param.effectiveName()

        // Wrap in TokenNode if annotated
        val currentParent = param.preserveToken(parent)
        val node = LibSpecNode.ParameterNode(currentParent, name, param)
        currentParent.children += node

        // If the parameter type is a class, recurse
        val decl = param.type.resolve().declaration as? KSClassDeclaration
        if (decl?.qualifiedName?.getQualifier()?.startsWith("kotlin") == false) handleClassDecl(decl, node)
    }

    // 2. Handle a class declaration (sealed, token, or plain)
    private fun handleClassDecl(
        decl: KSClassDeclaration,
        parent: LibSpecNode,
    ) {
        val currentParent = decl.preserveToken(parent)

        when {
            // Sealed container: iterate subclasses
            decl.isSealed() -> {
                decl.declarations.filterIsInstance<KSClassDeclaration>().forEach { sub ->
                    handleClassDecl(sub, currentParent)
                }
            }

            decl.isEnum() -> {
                decl.declarations.filterIsInstance<KSClassDeclaration>().filter {
                    it.classKind == ClassKind.ENUM_ENTRY
                }.forEach {
                    currentParent.children += LibSpecNode.TokenNode(currentParent, it.tokenName())
                }
            }

            decl.isDataObject() && !decl.hasAnnotation<RedisOption.Token>() -> {
                val tokName = decl.simpleName.asString() // token case handled in #preserveToken
                val tokNode = LibSpecNode.TokenNode(currentParent, tokName)
                currentParent.children += tokNode
            }
            // Plain class: descend into ctor params
            decl.classKind == ClassKind.CLASS -> {
                decl.primaryConstructor?.parameters?.forEach { handleValueParam(it, currentParent) }
            }
        }
    }

    private fun KSAnnotated.preserveToken(givenNode: LibSpecNode): LibSpecNode =
        getAnnotation<RedisOption.Token>()?.get("name")?.let { tokenName ->
            val tokNode = LibSpecNode.TokenNode(parent = givenNode, name = tokenName)
            givenNode.children += tokNode
            tokNode
        } ?: givenNode
}

