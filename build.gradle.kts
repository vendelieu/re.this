allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    group = "eu.vendeli"
    version = providers.gradleProperty("libVersion").getOrElse("dev")
}

disableUnreachableTasks()

plugins {
    alias(libs.plugins.ktlinter) apply false
    alias(libs.plugins.deteKT) apply false
}
