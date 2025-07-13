configureKotlin {
    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.io.core)
        implementation(libs.ktor.utils)
        implementation(libs.bignum)
    }
}

