plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serde)
}

group = "eu.vendeli"


dependencies {
    implementation(project(":shared"))
    implementation(project(":client"))
    implementation(libs.ksp)
    implementation(libs.poet)
    implementation(libs.poet.ksp)
    implementation(libs.serde.json)
    implementation(libs.autoService.annotations)
    ksp(libs.autoService.ksp)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(22)
}
