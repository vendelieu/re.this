plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.publisher)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
