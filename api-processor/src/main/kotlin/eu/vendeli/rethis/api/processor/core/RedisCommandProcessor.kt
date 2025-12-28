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
import eu.vendeli.rethis.shared.annotations.RedisCommand

class RedisCommandProcessor(
    internal val logger: KSPLogger,
    options: Map<String, String>,
) : SymbolProcessor {
    internal val clientDir = options["clientProjectDir"]!!
    internal val codecsPackage = "eu.vendeli.rethis.codecs"
    internal val commandPackage = "eu.vendeli.rethis.command"

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val resolvedCommands: Map<RCommandData, List<KSClassDeclaration>> =
            resolver.getSymbolsWithAnnotation(RedisCommand::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .groupBy { it.getCommandData() }

        // DEBUG: Log ALL found command specs
        logger.warn("=== KSP FOUND ${resolvedCommands.size} COMMAND SPECS ===")
        resolvedCommands.forEach { (cmd, klass) ->
            logger.warn("  - ${klass.joinToString { it.simpleName.toString() }} -> ${cmd.name}")
        }
        logger.warn("=== END OF FOUND SPECS ===")

        if (resolvedCommands.isEmpty()) return emptyList()

        context += ResolvedSpecs(resolvedCommands)
        loadGlobalCtx()

        // Iterate over each command and ALL its specs
        context.resolvedSpecs.spec.forEach { (commandData, klassList) ->
            klassList.forEach { klass ->
                (commandData to klass).loadCommandCtx()
                process(commandData)
                context.clearPerCommand()
            }
        }

        context.proceedOnFinish()
        context.clearAll()

        return emptyList()
    }

    internal companion object {
        val context = ProcessorContext()
    }
}
