package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.FileSpec

/**
 * Creates the file for [spec] and writes it. Any failure is reported through [logger] as an error,
 * naming the file and the cause, so the processing round fails instead of silently dropping the file.
 */
internal fun writeGeneratedFile(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    dependencies: Dependencies,
    spec: FileSpec,
) {
    runCatching {
        codeGenerator.createNewFile(dependencies, spec.packageName, spec.name)
            .bufferedWriter()
            .use { spec.writeTo(it) }
    }.onFailure {
        logger.error(
            "Failed to write generated file ${spec.packageName}.${spec.name}: ${it.stackTraceToString()}",
        )
    }
}
