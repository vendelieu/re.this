package eu.vendeli.rethis.api.processor.types

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.*
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class LibSpecNode {
    open val parent: LibSpecNode? = null
    abstract val symbol: KSAnnotated
    open val children: MutableList<LibSpecNode> = mutableListOf()

    class TokenNode(
        override val parent: LibSpecNode? = null,
        val name: String,
        override val symbol: KSAnnotated,
        override val children: MutableList<LibSpecNode> = mutableListOf(),
    ) : LibSpecNode()

    class ParameterNode(
        override val parent: LibSpecNode? = null,
        val name: String,
        override val symbol: KSValueParameter,
        override val children: MutableList<LibSpecNode> = mutableListOf(),
    ) : LibSpecNode()

    class ContainerNode(
        override val parent: LibSpecNode? = null,
        override val symbol: KSDeclaration,
        override val children: MutableList<LibSpecNode> = mutableListOf(),
    ) : LibSpecNode()
}

/**
 * Starting from this node, recursively spit out the first ParameterNode
 * whose `.name` equals the given name.
 */
fun LibSpecNode.findParameterByName(name: String): LibSpecNode.ParameterNode? {
    // 1) If I’m a ParameterNode and my name matches, return me
    if (this is LibSpecNode.ParameterNode && this.name == name) return this

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
fun LibSpecNode.findTokenByName(token: String): LibSpecNode.TokenNode? {
    if (this is LibSpecNode.TokenNode && this.name == token) return this
    for (child in this.children) {
        val found = child.findTokenByName(token)
        if (found != null) return found
    }
    return null
}


object LibSpecTreeBuilder {
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

        // Preserve any @RedisOption.Token on the parameter itself
        val currentParent = param.preserveToken(parent)

        // Create the ParameterNode with proper order
        val paramNode = LibSpecNode.ParameterNode(currentParent, name, param)
        currentParent.children += paramNode

        // Recurse into its nested type (for sealed enums, data objects, etc.)
        handleDeclaration(param.type.resolve(), paramNode)
    }

    private fun handleDeclaration(
        pType: KSType,
        parent: LibSpecNode,
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
                    currentParent.children += LibSpecNode.TokenNode(
                        parent = currentParent,
                        name = entry.tokenName(),
                        symbol = entry,
                    )
                }
            // Data object without explicit token
            decl.isDataObject() && !decl.hasAnnotation<RedisOption.Token>() -> {
                currentParent.children += LibSpecNode.TokenNode(
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
        givenNode: LibSpecNode,
    ): LibSpecNode = getAnnotationsByType(RedisOption.Token::class).map {
        val tokNode = LibSpecNode.TokenNode(
            parent = givenNode,
            name = it.name,
            symbol = this,
        )
        givenNode.children += tokNode
        tokNode
    }.lastOrNull() ?: givenNode
}
