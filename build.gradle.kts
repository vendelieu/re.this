@file:Suppress("PropertyName")

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SonatypeHost
import kotlinx.validation.ExperimentalBCVApi
import org.gradle.internal.impldep.org.apache.commons.lang.SystemUtils.OS_NAME
import org.gradle.internal.impldep.org.eclipse.jgit.transport.SshConstants.HOST_NAME
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.LocalDate

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.deteKT)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlinter)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlin.binvalid)
    alias(libs.plugins.kover)
}

group = "eu.vendeli.re.this"
description = "Lightweight, coroutine-based Redis client for Kotlin Multiplatform"
version = providers.gradleProperty("libVersion").getOrElse("dev")

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs = listOf("-opt-in=eu.vendeli.rethis.annotations.ReThisInternal")
    }

    val jvmTargetVer = 11
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

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.network)
                implementation(libs.kotlinx.io.core)

                api(libs.bignum)
                api(libs.coroutines.core)
                api(libs.kotlinx.datetime)
            }
        }

        jvmTest {
            dependencies {
                implementation(libs.kotlin.reflect)
                implementation(libs.test.kotest.junit5)
                implementation(libs.test.kotest.assertions)
                implementation(libs.logback)
                implementation(libs.testcontainer.redis)
            }
        }
    }
    jvmToolchain(jvmTargetVer)
}

repositories {
    mavenCentral()
}

mavenPublishing {
    coordinates("eu.vendeli", project.name, project.version.toString())
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, true)
    val javaDoc = if (providers.gradleProperty("signing.keyId").isPresent) {
        signAllPublications()

        JavadocJar.Dokka("dokkaHtml")
    } else JavadocJar.Empty()

    configure(KotlinMultiplatform(javaDoc, true))

    pom {
        name = project.name
        description = project.description
        inceptionYear = "2024"
        url = "https://github.com/vendelieu/re.this"

        licenses {
            license {
                name = "Apache 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "Vendelieu"
                name = "Vendelieu"
                email = "vendelieu@gmail.com"
                url = "https://vendeli.eu"
            }
        }
        scm {
            connection = "scm:git:github.com/vendelieu/re.this.git"
            developerConnection = "scm:git:ssh://github.com/vendelieu/re.this.git"
            url = "https://github.com/vendelieu/re.this.git"
        }
        issueManagement {
            system = "Github"
            url = "https://github.com/vendelieu/re.this/issues"
        }
    }
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

val OS_NAME = System.getProperty("os.name").lowercase()
val HOST_NAME: String = when {
    OS_NAME.startsWith("linux") -> "linux"
    OS_NAME.startsWith("windows") -> "windows"
    OS_NAME.startsWith("mac") -> "macos"
    else -> error("Unknown os name `$OS_NAME`")
}
fun isAvailableForPublication(publication: Publication): Boolean {
    val name = publication.name
    if (name == "maven") return true

    var result = false
    val jvmAndCommon = setOf(
        "jvm",
        "androidRelease",
        "androidDebug",
        "js",
        "wasmJs",
        "metadata",
        "kotlinMultiplatform"
    )
    result = result || name in jvmAndCommon
    result = result || (HOST_NAME == "linux" && (name == "linuxX64" || name == "linuxArm64"))
    result = result || (HOST_NAME == "windows" && name == "mingwX64")
    val macPublications = setOf(
        "iosX64",
        "iosArm64",
        "iosSimulatorArm64",

        "watchosX64",
        "watchosArm32",
        "watchosArm64",
        "watchosSimulatorArm64",
        "watchosDeviceArm64",

        "tvosX64",
        "tvosArm64",
        "tvosSimulatorArm64",

        "macosX64",
        "macosArm64"
    )

    result = result || (HOST_NAME == "macos" && name in macPublications)

    return result
}


tasks {
    withType<Test> { useJUnitPlatform() }
    withType<AbstractPublishToMaven>().configureEach {
        onlyIf { isAvailableForPublication(publication) }
    }
    dokkaHtml.configure {
        outputDirectory = layout.buildDirectory.asFile.orNull?.resolve("dokka")
        dokkaSourceSets {
            collectionSchema.elements.forEach { _ -> moduleName = "re.this" }
        }
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            customAssets = listOf(rootDir.resolve("assets/logo-icon.svg"))
            footerMessage = "© ${LocalDate.now().year} Vendelieu"
        }
    }
}

apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib.enabled = true
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.from(files("$rootDir/detekt.yml"))
}
