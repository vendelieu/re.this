plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnly(project(":shared"))
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
