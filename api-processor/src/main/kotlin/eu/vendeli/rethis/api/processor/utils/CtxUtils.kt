package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import eu.vendeli.rethis.api.processor.context.*
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.RCommandData
import eu.vendeli.rethis.api.processor.utils.RedisSpecLoader.loadSpecs
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal val ProcessorContext.currentCommand get() = this[CurrentCommand]!!
internal val ProcessorContext.logger get() = this[Logger]!!.logger
internal val ProcessorContext.meta get() = this[ProcessorMeta]!!
internal val ProcessorContext.validation get() = this[ValidationResult]!!
internal val ProcessorContext.responses get() = this[SpecResponses]!!
internal val ProcessorContext.resolvedSpecs get() = this[ResolvedSpecs]!!

internal val ProcessorContext.curImports get() = this[Imports]!!.imports
internal val ProcessorContext.rSpec get() = this[RSpecRaw]!!.specs
internal val ProcessorContext.currentRSpec get() = rSpec[currentCommand.command.name]!!

internal val ProcessorContext.typeSpec get() = this[CodecObjectTypeSpec]!!.typeSpec
internal val ProcessorContext.fileSpec get() = this[CodecFileSpec]!!.fileSpec
internal val ProcessorContext.enrichedTree get() = this[ETree]!!.tree

internal fun addImport(vararg import: String) {
    context.curImports += import
}

internal fun reportError(command: String, error: String) {
    context.validation.reportError(command, error)
}

internal fun RCommandData.reportError(error: String) {
    reportError(name, error)
}

internal fun CurrentCommand.reportError(error: String) {
    reportError(command.name, error)
}

internal fun addProcessedResponses(cmd: String, responses: List<RespCode>) {
    context.responses.addProcessedResponses(cmd, responses)
}

internal fun Map.Entry<RCommandData, KSClassDeclaration>.loadCommandCtx() {
    context += CurrentCommand(key, value)
    context += Imports()
}

internal fun RedisCommandProcessor.loadGlobalCtx() {
    context += Logger(logger)
    loadSpecs()

    context += Logger(logger)
    context += ProcessorMeta(
        clientDir = clientDir,
        codecsPackage = codecsPackage,
        commandPackage = commandPackage,
    )
    context += ValidationResult()
}

internal fun getByCommandsByName(name: String): List<Map.Entry<RCommandData, KSClassDeclaration>>? {
    return context.resolvedSpecs.groupedSpecs[name] ?: emptyList()
}
