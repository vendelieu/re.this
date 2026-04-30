@file:Suppress("PropertyName")

import java.time.Duration
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

tasks.withType<Test> {
    useJUnitPlatform()
    timeout.set(Duration.ofMinutes(20))
}

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

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude { it.file.absolutePath.replace('\\', '/').contains("/build/generated/") }
    reports {
        sarif.required.set(true)
    }
}

// The default `detekt` lifecycle task is NO-SOURCE on KMP — aggregate per-target tasks
// (detektJvmMain, detektMetadataCommonMain, ...) into a single entrypoint for CI.
val detektAll by tasks.registering {
    group = "verification"
    description = "Runs detekt for all Kotlin source sets in this module."
    dependsOn(tasks.withType<io.gitlab.arturbosch.detekt.Detekt>())
}

kotlinter {
    reporters = arrayOf("checkstyle", "sarif")
}

// Ensure platform compilation, source jar, lint, and detekt tasks run after KSP generates common metadata sources.
val kspTaskName = "kspCommonMainKotlinMetadata"
fun Task.shouldDependOnKsp(): Boolean =
    name != kspTaskName &&
        (
            name.startsWith("compileKotlin") ||
                name.contains("SourcesJar", ignoreCase = true) ||
                name.startsWith("lintKotlin") ||
                name.startsWith("formatKotlin") ||
                name.startsWith("detekt")
            )

tasks.matching { it.shouldDependOnKsp() }
    .configureEach {
        dependsOn(kspTaskName)
    }

tasks.withType<LintTask>().configureEach {
    exclude { it.file.absolutePath.replace('\\', '/').contains("/build/generated/") }
}

tasks.withType<FormatTask>().configureEach {
    exclude { it.file.absolutePath.replace('\\', '/').contains("/build/generated/") }
}

