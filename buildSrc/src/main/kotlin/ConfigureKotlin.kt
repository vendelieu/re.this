import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension


fun Project.configureKotlin(block: KotlinMultiplatformExtension.() -> Unit) {
    plugins.apply("kotlin-multiplatform")

    configure<KotlinMultiplatformExtension> {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs = listOf("-opt-in=eu.vendeli.rethis.annotations.ReThisInternal")
        }

        val jvmTargetVer = 17
        jvm {
            withJava()
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget = JvmTarget.fromTarget("$jvmTargetVer")
                        freeCompilerArgs = listOf("-Xjsr305=strict", "-opt-in=eu.vendeli.rethis.annotations.ReThisInternal")
                    }
                }
            }
        }
        jvmToolchain(jvmTargetVer)

        mingwX64()

        linuxX64()
        linuxArm64()

        macosX64()
        macosArm64()

        watchosX64()
        watchosArm32()
        watchosArm64()
        watchosSimulatorArm64()

        iosX64()
        iosArm64()
        iosSimulatorArm64()

        tvosX64()
        tvosArm64()
        tvosSimulatorArm64()

        androidNativeX64()
        androidNativeX86()
        androidNativeArm32()
        androidNativeArm64()

        targets.configureEach { disableCompilationsIfNeeded() }

        block()
    }

    disablePublicationTasksIfNeeded()
    disableUnreachableTasks()
}
