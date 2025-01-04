import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget

fun KotlinTarget.getHostType(): HostType? = when (platformType) {
    KotlinPlatformType.androidJvm,
    KotlinPlatformType.jvm,
    KotlinPlatformType.js,
    KotlinPlatformType.wasm,
        -> HostType.LINUX

    KotlinPlatformType.native -> when {
        name.startsWith("linux") -> HostType.LINUX

        name.startsWith("mingw") -> HostType.WINDOWS

        name.startsWith("ios") -> HostType.MAC_OS
        name.startsWith("watchos") -> HostType.MAC_OS
        name.startsWith("macos") -> HostType.MAC_OS
        name.startsWith("tvos") -> HostType.MAC_OS
        name.startsWith("android") -> HostType.LINUX
        else -> error("Unsupported native target: $this")
    }

    KotlinPlatformType.common -> null
}

enum class HostType {
    MAC_OS, LINUX, WINDOWS
}

fun KotlinTarget.isCompilationAllowed(): Boolean {
    if ((name == KotlinMetadataTarget.METADATA_TARGET_NAME) || !CommonParams.releaseMode) {
        return true
    }

    val os = OperatingSystem.current()

    return when (getHostType()) {
        HostType.MAC_OS -> os.isMacOsX
        HostType.LINUX -> os.isLinux
        HostType.WINDOWS -> os.isWindows
        null -> true
    }
}

fun KotlinTarget.disableCompilationsIfNeeded() {
    if (!isCompilationAllowed()) disableCompilations()
}

fun Project.disablePublicationTasksIfNeeded() {
    val targets = extensions.getByType<KotlinMultiplatformExtension>().targets

    tasks.withType<AbstractPublishToMaven>().configureEach {
        if (!isAllowed(targets)) {
            enabled = false
        }
    }
}

private fun KotlinTarget.disableCompilations() {
    compilations.configureEach { compileTaskProvider.get().enabled = false }
}

fun AbstractPublishToMaven.isAllowed(targets: NamedDomainObjectCollection<KotlinTarget>): Boolean {
    val publicationName: String? = publication?.name

    return when {
        publicationName == "kotlinMultiplatform" -> !CommonParams.releaseMode || CommonParams.metadataOnly

        CommonParams.metadataOnly -> false

        publicationName != null -> {
            val target = targets.find { it.name.startsWith(publicationName) }
            checkNotNull(target) { "Target not found for publication $publicationName" }
            target.isCompilationAllowed()
        }

        else -> {
            val target = targets.find { name.contains(other = it.name, ignoreCase = true) }
            checkNotNull(target) { "Target not found for publication $this" }
            target.isCompilationAllowed()
        }
    }
}
