plugins {
    dokka
    publish
}

configureKotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.io.core)
        implementation(libs.ktor.utils)
        implementation(libs.bignum)
    }
}

libraryData {
    name = "shared"
    description = "Shared Kotlin Multiplatform code for Rethis"
}
