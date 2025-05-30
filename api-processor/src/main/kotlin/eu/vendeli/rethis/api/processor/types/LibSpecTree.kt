package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.*
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class LibSpecTree {
    open val parent: LibSpecTree? = null
    abstract val symbol: KSAnnotated
    open val children: MutableList<LibSpecTree> = mutableListOf()
    var validated: Boolean = false

    data class TokenNode(
        override val parent: LibSpecTree? = null,
        val name: String,
        override val symbol: KSAnnotated,
        override val children: MutableList<LibSpecTree> = mutableListOf(),
    ) : LibSpecTree()

    data class ParameterNode(
        override val parent: LibSpecTree? = null,
        val name: String,
        override val symbol: KSValueParameter,
        override val children: MutableList<LibSpecTree> = mutableListOf(),
    ) : LibSpecTree()

    data class ContainerNode(
        override val parent: LibSpecTree? = null,
        override val symbol: KSDeclaration,
        override val children: MutableList<LibSpecTree> = mutableListOf(),
    ) : LibSpecTree()
}

/**
 * Starting from this node, recursively spit out the first ParameterNode
 * whose `.name` equals the given name.
 */
fun LibSpecTree.findParameterByName(name: String): LibSpecTree.ParameterNode? {
    // 1) If I’m a ParameterNode and my name matches, return me
    if (this is LibSpecTree.ParameterNode && this.name == name && !validated) return this

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


object LibSpecTreeBuilder {
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

        // Preserve any @RedisOption.Token on the parameter itself
        val currentParent = param.preserveToken(parent)

        // Create the ParameterNode with proper order
        val paramNode = LibSpecTree.ParameterNode(currentParent, name, param)
        currentParent.children += paramNode

        // Recurse into its nested type (for sealed enums, data objects, etc.)
        handleDeclaration(param.type.resolve(), paramNode)
    }

    private fun handleDeclaration(
        pType: KSType,
        parent: LibSpecTree,
    ) {
        // 1) Recurse into generic arguments first
        pType.arguments.forEach { arg ->
            arg.type?.resolve()?.let {
                handleDeclaration(it, parent)
            }
        }

        // 2) If the type is a KSClassDeclaration, handle special cases
        val decl = pType.declaration.safeCast<KSClassDeclaration>() ?: return
        // Skip Kotlin stdlib types
        if (decl.isStdType()) return

        // Preserve any token annotation on the type
        val currentParent = decl.preserveToken(parent)

        when {
            // Sealed class: each subclass becomes its own TokenNode
            decl.isSealed() -> decl.getSealedSubclasses().forEach { sub ->
                handleDeclaration(sub.asStarProjectedType(), currentParent)
            }
            // Enum: each enum entry becomes a TokenNode
            decl.isEnum() -> decl.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .forEach { entry ->
                    currentParent.children += LibSpecTree.TokenNode(
                        parent = currentParent,
                        name = entry.tokenName(),
                        symbol = entry,
                    )
                }
            // Data object without explicit token
            decl.isDataObject() && !decl.hasAnnotation<RedisOption.Token>() -> {
                currentParent.children += LibSpecTree.TokenNode(
                    parent = currentParent,
                    name = decl.effectiveName(),
                    symbol = decl,
                )
            }
            // Plain class: descend into ctor params
            decl.classKind == ClassKind.CLASS -> decl.getConstructors().flatMap {
                it.parameters
            }.forEach {
                if (!it.isVal) println("Parameter `${it.name?.asString()}` in `${it.parent?.parent}` should be val")
                handleValueParam(it, currentParent)
            }
        }
    }

    @OptIn(KspExperimental::class)
    private fun KSAnnotated.preserveToken(
        givenNode: LibSpecTree,
    ): LibSpecTree = getAnnotationsByType(RedisOption.Token::class).map {
        val tokNode = LibSpecTree.TokenNode(
            parent = givenNode,
            name = it.name,
            symbol = this,
        )
        givenNode.children += tokNode
        tokNode
    }.lastOrNull() ?: givenNode
}
