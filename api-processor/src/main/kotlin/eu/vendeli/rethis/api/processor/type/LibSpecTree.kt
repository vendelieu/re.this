package eu.vendeli.rethis.api.processor.type

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.*
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class LibSpecTree {
    open val parent: LibSpecTree? = null
    open val children: MutableList<LibSpecTree> = mutableListOf()

    data class TokenNode(
        override val parent: LibSpecTree? = null,
        val name: String,
        override val children: MutableList<LibSpecTree> = mutableListOf(),
    ) : LibSpecTree()

    data class ParameterNode(
        override val parent: LibSpecTree? = null,
        val name: String,
        val symbol: KSValueParameter,
        override val children: MutableList<LibSpecTree> = mutableListOf(),
    ) : LibSpecTree()

    data class ContainerNode(
        override val parent: LibSpecTree? = null,
        val symbol: KSDeclaration,
        override val children: MutableList<LibSpecTree> = mutableListOf(),
    ) : LibSpecTree()
}

/**
 * Find a ParameterNode anywhere in the tree by its effective name.
 */
fun LibSpecTree.findParameterByName(name: String, root: LibSpecTree = this): LibSpecTree.ParameterNode? = when (root) {
    is LibSpecTree.ParameterNode -> if (root.name == name) root else null
    else -> root.children.asSequence()
        .mapNotNull { findParameterByName(name, it) }
        .firstOrNull()
}

/**
 * Find a TokenNode anywhere in the tree by its token name.
 */
fun LibSpecTree.findTokenByName(token: String, root: LibSpecTree = this): LibSpecTree.TokenNode? = when (root) {
    is LibSpecTree.TokenNode -> if (root.name == token) root else null
    else -> root.children.asSequence()
        .mapNotNull { findTokenByName(token, it) }
        .firstOrNull()
}


object SpecTreeBuilder {
    fun build(function: KSFunctionDeclaration): LibSpecTree.ContainerNode {
        val root = LibSpecTree.ContainerNode(parent = null, symbol = function)
        function.parameters.forEach { handleValueParam(it, root) }
        return root
    }

    // 1. Handle a value parameter
    private fun handleValueParam(
        param: KSValueParameter,
        parent: LibSpecTree,
    ) {
        // Determine name override or default
        val name = param.effectiveName()

        // Wrap in TokenNode if annotated
        val currentParent = param.preserveToken(parent)
        val node = LibSpecTree.ParameterNode(currentParent, name, param)
        currentParent.children += node

        // If the parameter type is a class, recurse
        // type may be array/collection, especially when it's varargs involved
        handleParamWithArguments(param.type.resolve(), currentParent)
    }

    private fun handleParamWithArguments(pType: KSType, parent: LibSpecTree) {
        pType.arguments.forEach {  // handle more nested types
            handleParamWithArguments(it.type?.resolve() ?: return@forEach, parent)
        }
        val declaration = pType.declaration.safeCast<KSClassDeclaration>() ?: return
        if (declaration.qualifiedName?.getQualifier()?.startsWith("kotlin") == false) {
            handleClassDecl(declaration, parent)
        }
    }

    // 2. Handle a class declaration (sealed, token, or plain)
    private fun handleClassDecl(
        decl: KSClassDeclaration,
        parent: LibSpecTree,
    ) {
        val currentParent = decl.preserveToken(parent)

        when {
            // Sealed container: iterate subclasses
            decl.isSealed() -> {
                decl.getSealedSubclasses().forEach { sub ->
                    handleClassDecl(sub, currentParent)
                }
            }

            decl.isEnum() -> {
                decl.declarations.filterIsInstance<KSClassDeclaration>().filter {
                    it.classKind == ClassKind.ENUM_ENTRY
                }.forEach {
                    currentParent.children += LibSpecTree.TokenNode(currentParent, it.tokenName())
                }
            }

            decl.isDataObject() && !decl.hasAnnotation<RedisOption.Token>() -> {
                val tokName = decl.simpleName.asString() // token case handled in #preserveToken
                val tokNode = LibSpecTree.TokenNode(currentParent, tokName)
                currentParent.children += tokNode
            }
            // Plain class: descend into ctor params
            decl.classKind == ClassKind.CLASS -> {
                decl.getConstructors().flatMap { it.parameters }.forEach {
                    handleValueParam(it, currentParent)
                }
            }
        }
    }

    private fun KSAnnotated.preserveToken(givenNode: LibSpecTree): LibSpecTree =
        getAnnotation<RedisOption.Token>()?.get("name")?.let { tokenName ->
            val tokNode = LibSpecTree.TokenNode(parent = givenNode, name = tokenName)
            givenNode.children += tokNode
            tokNode
        } ?: givenNode
}

