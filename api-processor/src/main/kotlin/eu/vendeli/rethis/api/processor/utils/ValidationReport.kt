package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.processing.KSPLogger

/**
 * Reports every collected spec validation error through `logger.error`.
 * KSP fails the round after processing finishes, so all mismatches are listed in a single run.
 */
internal fun reportValidationErrors(errors: Map<String, List<String>>, logger: KSPLogger) {
    errors.forEach { (command, messages) ->
        messages.forEach { logger.error("Spec validation failed for '$command': $it") }
    }
}
