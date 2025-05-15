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
 * Starting from this node, recursively spit out the first ParameterNode
 * whose `.name` equals the given name.
 */
fun LibSpecTree.findParameterByName(name: String): LibSpecTree.ParameterNode? {
    // 1) If I’m a ParameterNode and my name matches, return me
    if (this is LibSpecTree.ParameterNode && this.name == name) return this

    // 2) Otherwise, search each child in turn
    for (child in this.children) {
        val found = child.findParameterByName(name)
        if (found != null) return found
    }
    return null
}

/**
 * Same idea, but for TokenNode by its token literal.
 */
fun LibSpecTree.findTokenByName(token: String): LibSpecTree.TokenNode? {
    if (this is LibSpecTree.TokenNode && this.name == token) return this
    for (child in this.children) {
        val found = child.findTokenByName(token)
        if (found != null) return found
    }
    return null
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
        val paramNode = LibSpecTree.ParameterNode(currentParent, name, param)
        currentParent.children += paramNode

        // If the parameter type is a class, recurse
        // type may be array/collection, especially when it's varargs involved
        handleParamWithArguments(param.type.resolve(), paramNode)
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

