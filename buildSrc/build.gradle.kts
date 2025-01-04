plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.dokka)
    implementation(libs.publisher)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
