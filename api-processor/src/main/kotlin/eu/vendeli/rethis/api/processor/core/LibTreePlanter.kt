package eu.vendeli.rethis.api.processor.core

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import eu.vendeli.rethis.api.processor.context.ETree
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.*
import eu.vendeli.rethis.api.processor.utils.*
import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisMeta

internal object LibTreePlanter {
    fun prepare() {
        val currentCommand = context.currentCommand
        val root = EnrichedNode(
            attr = mutableSetOf(
                EnrichedTreeAttr.Symbol(currentCommand.encodeFunction, SymbolType.Function),
            ),
        )

        context += ETree(root)

        currentCommand.encodeFunction.parameters.forEach {
            handleParameter(it, root)
        }
    }

    @OptIn(KspExperimental::class)
    fun handleParameter(p: KSValueParameter, parent: EnrichedNode) {
        val paramNode = EnrichedNode(parent = parent)
        parent.children += paramNode

        if (p.hasAnnotation<RedisMeta.WithSizeParam>()) {
            paramNode.attr.add(EnrichedTreeAttr.SizeParam)
        }

        paramNode.attr.add(EnrichedTreeAttr.Symbol(p, SymbolType.ValueParam))
        paramNode.attr.add(EnrichedTreeAttr.Name(p.name!!.asString()))

        p.saveTokens(paramNode)

        val pType = p.type.resolve()
        paramNode.attr.add(EnrichedTreeAttr.Type(pType))
        if (p.isVararg || pType.isCollection()) paramNode.attr.add(
            EnrichedTreeAttr.Multiple(
                vararg = p.isVararg,
                collection = pType.isCollection(),
            ),
        )
        val parentOpt = parent.attr.filterIsInstance<EnrichedTreeAttr.Optional>().firstOrNull()
        paramNode.attr.add(
            EnrichedTreeAttr.Optional(
                inherited = parentOpt?.inherited ?: parentOpt?.local,
                local = when {
                    pType.isMarkedNullable -> OptionalityType.Nullable
                    p.isVararg -> OptionalityType.Vararg
                    else -> null
                },
            ),
        )

        if (!pType.declaration.isStdType()) handleDeclaration(pType, paramNode)

        val parentBounds = parent.rangeBounds().second.path
        val nodesByName = context.currentRSpec.allNodes.filter { it.normalizedName == p.effectiveName() }

        val rNode = when {
            nodesByName.size == 1 -> nodesByName.first()
            nodesByName.size > 1 -> nodesByName.singleOrNull { it.path.isWithinBounds(parentBounds) }
            else -> null
        }

        if (rNode == null) {
            if (!p.hasAnnotation<RIgnoreSpecAbsence>()) {
                context.logger.warn("Param `${p.effectiveName()}` not found in RSpec [${context.currentCommand.command.name}, ${context.currentCommand.klass.simpleName.asString()}]")
            }
            return
        }
        paramNode.attr.add(EnrichedTreeAttr.RelatedRSpec(rNode))
        if (rNode.arg.keySpecIndex != null) paramNode.attr.add(EnrichedTreeAttr.Key)
    }

    @OptIn(KspExperimental::class)
    fun handleDeclaration(type: KSType, parent: EnrichedNode) {
        type.arguments.forEach { arg ->
            arg.type?.resolve()?.let {
                handleDeclaration(it, parent)
            }
        }

        val node = EnrichedNode(parent = parent)
        parent.children += node
        node.attr.add(EnrichedTreeAttr.Type(type))
        node.attr.add(EnrichedTreeAttr.Symbol(type.declaration, SymbolType.Class))

        type.declaration.saveTokens(node)
        val decl = type.declaration.safeCast<KSClassDeclaration>()

        when {
            type.declaration.isSealed() -> type.declaration
                .safeCast<KSClassDeclaration>()
                ?.getSealedSubclasses()
                ?.forEach { sub ->
                    handleDeclaration(sub.asStarProjectedType(), node)
                }

            type.declaration.isEnum() -> decl?.declarations
                ?.filterIsInstance<KSClassDeclaration>()
                ?.filter { it.classKind == ClassKind.ENUM_ENTRY }
                ?.forEach {
                    it.saveDeclarationToken(node)
                }

            type.declaration.isDataObject() -> {
                type.declaration.safeCast<KSClassDeclaration>()?.saveDeclarationToken(node)
            }

            decl?.classKind == ClassKind.CLASS -> decl.primaryConstructor?.parameters?.forEach {
                if (!it.isVal) context.logger.error("Parameter `${it.name?.asString()}` in `${it.parent?.parent}` should be val")
                handleParameter(it, node)
            }
        }
    }

    private fun KSClassDeclaration.saveDeclarationToken(node: EnrichedNode) {
        val name = tokenName()
        val rSpec = context.currentRSpec.allNodes.find { it.arg.token == name } ?: run {
            context.logger.warn("Token `$name` not found in RSpec [${context.currentCommand.command.name}]")
            return
        }

        node.attr.add(EnrichedTreeAttr.RelatedRSpec(rSpec))
        node.attr.add(EnrichedTreeAttr.Token(name, rSpec.arg.multipleToken))
    }
}
