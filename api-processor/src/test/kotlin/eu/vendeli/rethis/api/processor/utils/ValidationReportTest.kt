package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.processing.KSPLogger
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationReportTest {
    private val errors = mutableListOf<String>()
    private val warnings = mutableListOf<String>()

    private val logger = Proxy.newProxyInstance(
        KSPLogger::class.java.classLoader,
        arrayOf(KSPLogger::class.java),
    ) { _, method, args ->
        when (method.name) {
            "error" -> errors += args[0] as String
            "warn" -> warnings += args[0] as String
        }
        null
    } as KSPLogger

    @Test
    fun `reports each validation error through error naming the command and message`() {
        val found = mapOf(
            "HPERSIST" to listOf("Operation mismatch: declared 'READ'"),
            "BLPOP" to listOf("Blocking status mismatch: a", "Blocking status mismatch: b"),
        )

        reportValidationErrors(found, logger)

        assertEquals(3, errors.size)
        assertTrue(warnings.isEmpty())
        assertTrue("HPERSIST" in errors[0] && "Operation mismatch" in errors[0])
        assertTrue(errors.drop(1).all { "BLPOP" in it })
    }

    @Test
    fun `reports nothing when there are no validation errors`() {
        reportValidationErrors(emptyMap(), logger)

        assertTrue(errors.isEmpty())
        assertTrue(warnings.isEmpty())
    }
}
