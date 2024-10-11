@file:Suppress("PropertyName")

import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
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
version = System.getenv("libVersion") ?: "dev"

repositories {
    mavenCentral()
}

configureKotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.network)
            implementation(libs.kotlinx.io.core)

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
            implementation("commons-io:commons-io:2.17.0")
            implementation("org.apache.commons:commons-compress:1.27.1")
            implementation("com.fasterxml.woodstox:woodstox-core:7.0.0")
        }
    }
}

buildscript {
    dependencies.classpath(libs.dokka.base)
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

@OptIn(ExperimentalBCVApi::class)
apiValidation.klib.enabled = true

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.from(files("$rootDir/detekt.yml"))
}
