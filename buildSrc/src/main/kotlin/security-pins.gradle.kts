import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

/**
 * Forces patched versions of transitive dependencies flagged by security advisories.
 * Everything is driven by `gradle/libs.versions.toml`, so Renovate bumps are picked up with no script edits:
 *  - libraries named `pin-*` are pinned on every configuration of every project;
 *  - versions named `npm-*` become Yarn resolutions for the Kotlin/JS toolchain (test tooling only);
 *    the package name is the alias with `-` restored, so scoped/dotted npm names are not supported.
 * These pins only affect build/test tooling, never the dependencies of published artifacts.
 */
// Catalog aliases are normalized to dots (`pin-jackson-core` -> `pin.jackson.core`).
val pinPrefix = "pin."
val npmPrefix = "npm."

// Read lazily from the root project: a subproject's catalog extension isn't registered yet when
// `allprojects { apply(...) }` runs, but the root's is, and dependency resolution happens much later.
val catalog by lazy { rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs") }

val pinnedJvm: Map<String, String> by lazy {
    catalog.libraryAliases
        .filter { it.startsWith(pinPrefix) }
        .map { catalog.findLibrary(it).get().get() }
        .associate { "${it.module.group}:${it.module.name}" to it.versionConstraint.requiredVersion }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        pinnedJvm["${requested.group}:${requested.name}"]?.let { version ->
            useVersion(version)
            because("security pin from libs.versions.toml")
        }
    }
}

if (project === rootProject) {
    val pinnedNpm: Map<String, String> = catalog.versionAliases
        .filter { it.startsWith(npmPrefix) }
        .associate { it.removePrefix(npmPrefix).replace('.', '-') to catalog.findVersion(it).get().requiredVersion }

    plugins.withType<YarnPlugin> {
        extensions.configure<YarnRootExtension> {
            pinnedNpm.forEach { (name, version) -> resolution(name, version) }
        }
    }
}
