@file:Suppress("PropertyName")

import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import java.time.LocalDate

plugins {
    id("publish")
    alias(libs.plugins.deteKT)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlinter)
    alias(libs.plugins.kotlin.binvalid)
    alias(libs.plugins.kover)
}

group = "eu.vendeli.re.this"
description = "Kotlin Multiplatform Redis Client: coroutine-based, DSL-powered, and easy to use."
version = System.getenv("libVersion") ?: "dev"

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
