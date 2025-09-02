@file:Suppress("PropertyName")

import kotlinx.validation.ExperimentalBCVApi

plugins {
    alias(libs.plugins.deteKT)
    alias(libs.plugins.ktlinter)
    alias(libs.plugins.kotlin.binvalid)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.serde)
    dokka
    publish
}

configureKotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.network)
            implementation(libs.kotlinx.io.core)
            implementation(libs.serde.json.io)

            api(project(":shared"))
            api(libs.ktor.network.tls)
            api(libs.bignum)
            api(libs.coroutines.core)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlin.cotest)
            implementation(libs.test.kotest.junit5)
            implementation(libs.test.kotest.assertions)
            implementation(libs.logback)
            implementation(libs.testcontainers.redis)
        }
    }
}

libraryData {
    name = "rethis"
    description = "Kotlin Multiplatform Redis Client: coroutine-based, DSL-powered, and easy to use."
}

tasks.withType<Test> { useJUnitPlatform() }

@OptIn(ExperimentalBCVApi::class)
apiValidation.klib.enabled = true

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.from(files("$rootDir/detekt.yml"))
}
