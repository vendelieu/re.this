package eu.vendeli.rethis.api.processor.utils

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.FileSpec
import java.io.ByteArrayOutputStream
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeneratedFileWriterTest {
    private val errors = mutableListOf<String>()

    private val logger = Proxy.newProxyInstance(
        KSPLogger::class.java.classLoader,
        arrayOf(KSPLogger::class.java),
    ) { _, method, args ->
        if (method.name == "error") errors += args[0] as String
        null
    } as KSPLogger

    private fun generator(onCreate: () -> java.io.OutputStream) = Proxy.newProxyInstance(
        CodeGenerator::class.java.classLoader,
        arrayOf(CodeGenerator::class.java),
    ) { _, method, _ ->
        check(method.name == "createNewFile")
        onCreate()
    } as CodeGenerator

    private val spec = FileSpec.builder("com.example.codecs", "FooCodec").build()

    @Test
    fun `reports an error naming the file when file creation fails`() {
        val generator = generator { throw java.nio.file.FileAlreadyExistsException("boom") }

        writeGeneratedFile(generator, logger, Dependencies(aggregating = true), spec)

        assertEquals(1, errors.size)
        assertTrue("com.example.codecs" in errors.single())
        assertTrue("FooCodec" in errors.single())
        assertTrue("FileAlreadyExistsException" in errors.single())
    }

    @Test
    fun `reports nothing when the file is written`() {
        val out = ByteArrayOutputStream()
        val generator = generator { out }

        writeGeneratedFile(generator, logger, Dependencies(aggregating = true), spec)

        assertTrue(errors.isEmpty())
        assertTrue(out.toString().contains("package com.example.codecs"))
    }
}
