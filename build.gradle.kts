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

group = "eu.vendeli.rethis"
version = System.getenv("libVersion") ?: "dev"

repositories { mavenCentral() }

configureKotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.network)
            implementation(libs.kotlinx.io.core)
            implementation(libs.serde.json.io)

            api(libs.ktor.network.tls)
            api(libs.bignum)
            api(libs.coroutines.core)
            api(libs.kotlinx.datetime)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.test.kotest.junit5)
            implementation(libs.test.kotest.assertions)
            implementation(libs.logback)
            implementation("com.redis:testcontainers-redis:1.7.0") {
                exclude("commons-io", "commons-io")
                exclude("org.apache.commons", "commons-compress")
                exclude("com.fasterxml.woodstox", "woodstox-core")
            }
            implementation("commons-io:commons-io:2.19.0")
            implementation("org.apache.commons:commons-compress:1.27.1")
            implementation("com.fasterxml.woodstox:woodstox-core:7.1.0")
        }
    }
}

tasks {
    withType<Test> { useJUnitPlatform() }
}

@OptIn(ExperimentalBCVApi::class)
apiValidation.klib.enabled = true

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.from(files("$rootDir/detekt.yml"))
}
