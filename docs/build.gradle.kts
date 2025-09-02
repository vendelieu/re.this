plugins {
    kotlin("jvm") apply false
    dokka
}

dependencies {
    dokka(project(":client"))
    dokka(project(":shared"))
}
