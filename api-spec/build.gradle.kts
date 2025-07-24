import jdk.tools.jlink.resources.plugins

plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

group = "eu.vendeli.utils"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":api-spec-common"))
    ksp(project(":api-processor"))
}

ksp {
    arg(
        "clientProjectDir",
        rootDir.resolve("client/src/commonMain/kotlin/").absolutePath,
    )
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(22)
}
