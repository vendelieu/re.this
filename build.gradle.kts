allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    group = "eu.vendeli"
    version = providers.environmentVariable("libVersion").getOrElse("dev")

    // patched transitive versions for build/test tooling, sourced from libs.versions.toml
    apply(plugin = "security-pins")
}

plugins {
    alias(libs.plugins.ktlinter) apply false
    alias(libs.plugins.deteKT) apply false
}
