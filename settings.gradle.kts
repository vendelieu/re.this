rootProject.name = "rethis"

include(":api-spec")
include(":api-processor")
include(":client")
include(":shared")
include("benchmarks")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("docs")