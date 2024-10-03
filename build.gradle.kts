@file:Suppress("PropertyName")

import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDate

plugins {
    alias(libs.plugins.deteKT)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlinter)
    alias(libs.plugins.kotlin.binvalid)
    alias(libs.plugins.kover)
    id("publish")
}

group = "eu.vendeli.re.this"
description = "Lightweight, coroutine-based Redis client for Kotlin Multiplatform"
version = providers.gradleProperty("libVersion").getOrElse("dev")

repositories {
    mavenCentral()
}

configureKotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.network)
                implementation(libs.kotlinx.io.core)

                api(libs.bignum)
                api(libs.coroutines.core)
                api(libs.kotlinx.datetime)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.reflect)
                implementation(libs.test.kotest.junit5)
                implementation(libs.test.kotest.assertions)
                implementation(libs.logback)
                implementation(libs.testcontainer.redis)
            }
        }
    }
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

val OS_NAME = System.getProperty("os.name").lowercase()
val HOST_NAME: String = when {
    OS_NAME.startsWith("linux") -> "linux"
    OS_NAME.startsWith("windows") -> "windows"
    OS_NAME.startsWith("mac") -> "macos"
    else -> error("Unknown os name `$OS_NAME`")
}

fun isAvailableForPublication(publication: Publication): Boolean {
    val name = publication.name
    if (name == "maven") return true

    var result = false
    val jvmAndCommon = setOf(
        "jvm",
        "androidRelease",
        "androidDebug",
        "js",
        "wasmJs",
        "metadata",
        "kotlinMultiplatform",
    )
    result = result || name in jvmAndCommon
    result = result || (HOST_NAME == "linux" && (name == "linuxX64" || name == "linuxArm64"))
    result = result || (HOST_NAME == "windows" && name == "mingwX64")
    val macPublications = setOf(
        "iosX64",
        "iosArm64",
        "iosSimulatorArm64",

        "watchosX64",
        "watchosArm32",
        "watchosArm64",
        "watchosSimulatorArm64",
        "watchosDeviceArm64",

        "tvosX64",
        "tvosArm64",
        "tvosSimulatorArm64",

        "macosX64",
        "macosArm64",
    )

    result = result || (HOST_NAME == "macos" && name in macPublications)

    return result
}


tasks {
    withType<Test> { useJUnitPlatform() }
    dokkaHtml.configure {
        outputDirectory = layout.buildDirectory.asFile.orNull?.resolve("dokka")
        dokkaSourceSets {
            collectionSchema.elements.forEach { _ -> moduleName = "re.this" }
        }
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            customAssets = listOf(rootDir.resolve("assets/logo-icon.svg"))
            footerMessage = "© ${LocalDate.now().year} Vendelieu"
        }
    }
}

apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib.enabled = true
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.from(files("$rootDir/detekt.yml"))
}
