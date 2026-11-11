plugins {
    kotlin("jvm") apply false
    dokka
}

dependencies {
    dokka(project(":client"))
    dokka(project(":shared"))

    // multi-version docs: merges previously published versions (see `dokkaOlderVersionsDir`) into the site
    dokkaPlugin(libs.dokka.versioning)
}

/**
 * Directory holding the documentation of previously released versions, one sub-folder per version
 * (`<dir>/0.4.5/<dokka html output>`). The release workflow assembles it from `gh-pages` via
 * `scripts/prepare-older-docs.sh`; locally it is simply absent and only the current version is rendered.
 */
val dokkaOlderVersionsDir: Provider<Directory> = providers
    .gradleProperty("dokkaOlderVersionsDir")
    .map { layout.projectDirectory.dir(it) }
    .orElse(layout.buildDirectory.dir("dokka-older-versions"))

dokka {
    pluginsConfiguration.versioning {
        version.set(project.version.toString())
        olderVersionsDir.set(dokkaOlderVersionsDir.filter { it.asFile.isDirectory })
        renderVersionsNavigationOnAllPages.set(true)
    }
}
