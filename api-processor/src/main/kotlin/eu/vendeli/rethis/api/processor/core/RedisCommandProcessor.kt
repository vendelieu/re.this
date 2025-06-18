package eu.vendeli.rethis.api.processor.core

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import eu.vendeli.rethis.api.processor.context.ProcessorContext
import eu.vendeli.rethis.api.processor.context.ResolvedSpecs
import eu.vendeli.rethis.api.processor.core.RedisProcessor.process
import eu.vendeli.rethis.api.processor.types.RCommandData
import eu.vendeli.rethis.api.processor.types.getCommandData
import eu.vendeli.rethis.api.processor.utils.loadCommandCtx
import eu.vendeli.rethis.api.processor.utils.loadGlobalCtx
import eu.vendeli.rethis.api.processor.utils.resolvedSpecs
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand

class RedisCommandProcessor(
    internal val logger: KSPLogger,
    options: Map<String, String>,
) : SymbolProcessor {
    internal val clientDir = options["clientProjectDir"]!!
    internal val codecsPackage = "eu.vendeli.rethis.codecs"
    internal val commandPackage = "eu.vendeli.rethis.command"

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val resolvedCommands: Map<RCommandData, KSClassDeclaration> =
            resolver.getSymbolsWithAnnotation(RedisCommand::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .associateBy { it.getCommandData() }

        context += ResolvedSpecs(resolvedCommands)
        loadGlobalCtx()

        context.resolvedSpecs.spec.forEach {
            it.loadCommandCtx()

            process(it.key)

            context.clearPerCommand()
        }

        return emptyList()
    }

    internal companion object {
        val context = ProcessorContext()
    }
}
