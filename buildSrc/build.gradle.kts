plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.dokka)
    implementation(libs.publisher)
    // Applied by id (no version) in client; kept on this classpath so the pins below reach its transitive deps.
    implementation(libs.kover.gradlePlugin)

    // Plugin classpaths can't see the `libs` accessors in module build scripts, so constrain them here.
    val catalog = versionCatalogs.named("libs")
    catalog.libraryAliases.filter { it.startsWith("pin.") }.forEach { alias ->
        val pin = catalog.findLibrary(alias).get().get()
        constraints {
            implementation("${pin.module}:${pin.versionConstraint.requiredVersion}") {
                because("security pin from libs.versions.toml ($alias)")
            }
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
