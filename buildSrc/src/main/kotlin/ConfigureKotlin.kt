import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureKotlin(block: KotlinMultiplatformExtension.() -> Unit) {
    plugins.apply("kotlin-multiplatform")

    configure<KotlinMultiplatformExtension> {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            extraWarnings.set(true)
            freeCompilerArgs.addAll(
                "-Xwarning-level=CAN_BE_VAL:disabled",
                "-Xwarning-level=NOTHING_TO_INLINE:disabled",
                "-Xwarning-level=REDUNDANT_VISIBILITY_MODIFIER:disabled",
                "-opt-in=eu.vendeli.rethis.annotations.ReThisInternal",
                "-opt-in=eu.vendeli.rethis.annotations.ReThisExperimental",
                "-Xannotation-default-target=param-property",
                "-opt-in=kotlin.time.ExperimentalTime",
            )
        }

        val jvmTargetVer = 17
        jvm {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.fromTarget("$jvmTargetVer"))
                        freeCompilerArgs.addAll(
                            "-Xjsr305=strict",
                        )
                    }
                }
            }
        }
        jvmToolchain(jvmTargetVer)

        js { nodejs() }

        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            nodejs()
            d8()
        }

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

        block()
    }
}
