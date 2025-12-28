plugins {
    alias(libs.plugins.kotlin.binvalid)
    dokka
    publish
}

configureKotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.io.core)
        api(libs.kotlinx.io.bytestring)
        implementation(libs.ktor.utils)
        implementation(libs.bignum)
    }
}

libraryData {
    name = "rethis-shared"
    description = "Shared Kotlin Multiplatform code for Rethis"
}
