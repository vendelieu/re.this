rootProject.name = "rethis"

include(":api-spec")
include(":api-spec-common")
include(":api-processor")
include(":client")
include("benchmarks")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
