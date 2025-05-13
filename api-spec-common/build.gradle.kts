configureKotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.utils)
            implementation(libs.bignum)
        }
    }
}

