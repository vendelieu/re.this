import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    group = "eu.vendeli"
    version = providers.environmentVariable("libVersion").getOrElse("dev")

    // Build-tool classpaths (Dokka generator, KSP) pull in vulnerable transitive versions;
    // pin the patched releases. Nothing here is part of the published artifacts' dependencies.
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            when ("${requested.group}:${requested.name}") {
                "com.fasterxml.jackson.core:jackson-core",
                "com.fasterxml.jackson.core:jackson-databind",
                -> useVersion("2.22.3")

                "com.google.guava:guava" -> useVersion("33.7.2-jre")
                "org.freemarker:freemarker" -> useVersion("2.3.35")
            }
        }
    }
}

plugins {
    alias(libs.plugins.ktlinter) apply false
    alias(libs.plugins.deteKT) apply false
}

// Force patched versions of transitive npm test tooling (mocha & co.) flagged by Dependabot.
// Test-only dependencies of the JS/Wasm targets; nothing here is shipped in published artifacts.
private val patchedNpmPackages = mapOf(
    "brace-expansion" to "5.0.12",
    "js-yaml" to "4.3.2",
    "serialize-javascript" to "7.1.3",
    "glob" to "14.0.2",
    "minimatch" to "11.0.1",
    "diff" to "8.0.3",
)

plugins.withType<YarnPlugin> {
    extensions.configure<YarnRootExtension> {
        patchedNpmPackages.forEach { (name, version) -> resolution(name, version) }
    }
}
