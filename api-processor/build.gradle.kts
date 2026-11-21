plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serde)
}

group = "eu.vendeli"


dependencies {
    implementation(project(":shared"))
    implementation(libs.kotlinx.io.core)
    implementation(libs.ksp)
    implementation(libs.poet)
    implementation(libs.poet.ksp)
    implementation(libs.serde.json)
    implementation(libs.autoService.annotations)
    ksp(libs.autoService.ksp)

    testImplementation(kotlin("test"))
}

val specRef = libs.versions.redis.spec
val generatedResourcesDir = layout.buildDirectory.dir("generated/resources/specRef")

val generateSpecRef by tasks.registering {
    val ref = specRef
    val outputFile = generatedResourcesDir.map { it.file("redis_spec_ref.properties") }
    inputs.property("ref", ref)
    outputs.file(outputFile)
    doLast {
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText("ref=${ref.get()}\n")
        }
    }
}

sourceSets.main {
    resources.srcDir(generatedResourcesDir)
}

tasks.processResources {
    dependsOn(generateSpecRef)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(JVM_TARGET)
}
