@file:Suppress("PropertyName")

import kotlinx.validation.ExperimentalBCVApi
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.deteKT)
    alias(libs.plugins.ktlinter)
    alias(libs.plugins.kotlin.binvalid)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.serde)
    dokka
    publish
}

dependencies {
    add("kspCommonMainMetadata", project(":api-processor"))
}

ksp {
    arg(
        "clientProjectDir",
        layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin").get().asFile.absolutePath,
    )
}

configureKotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.network)
                implementation(libs.kotlinx.io.core)
                implementation(libs.serde.json.io)

                api(project(":shared"))
                api(libs.ktor.network.tls)
                api(libs.bignum)
                api(libs.coroutines.core)
            }
            // Include generated codecs and commands
            kotlin.srcDir(layout.buildDirectory.dir("generated/ksp/metadata/commonMain/kotlin"))
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.reflect)
            implementation(libs.kotlin.cotest)
            implementation(libs.test.kotest.junit5)
            implementation(libs.test.kotest.assertions)
            implementation(libs.logback)
            implementation(libs.testcontainers.redis)
            implementation(libs.test.dotenv.kotlin)
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

// Hide spec interfaces from public API - only expose generated functions
apiValidation {
    ignoredPackages.add("eu.vendeli.rethis.api.spec")
}

// Exclude spec interfaces from published artifacts
tasks.withType<org.gradle.jvm.tasks.Jar> {
    exclude("eu/vendeli/rethis/api/spec/**")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.from(files("$rootDir/detekt.yml"))
}

// Ensure platform compilation and source jar tasks run after KSP generates common metadata sources
val kspTaskName = "kspCommonMainKotlinMetadata"
fun Task.shouldDependOnKsp(): Boolean =
    name != kspTaskName &&
        (
            name.startsWith("compileKotlin") ||
                name.contains("SourcesJar", ignoreCase = true) ||
                name.startsWith("lintKotlin") ||
                name.startsWith("formatKotlin")
            )

tasks.matching { it.shouldDependOnKsp() }
    .configureEach {
        dependsOn(kspTaskName)
    }

// Exclude generated sources from kotlinter
tasks.withType<LintTask>().configureEach {
    exclude("**/build/generated/**")
}

tasks.withType<FormatTask>().configureEach {
    exclude("**/build/generated/**")
}

