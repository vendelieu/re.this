package eu.vendeli.rethis.api.processor.providers

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import eu.vendeli.rethis.api.processor.processors.RedisCommandProcessor

@AutoService(SymbolProcessorProvider::class)
class RedisCommandProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RedisCommandProcessor(
            environment.logger,
            environment.options
        )
    }
}
