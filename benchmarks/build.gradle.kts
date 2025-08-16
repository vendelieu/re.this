plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") version "2.2.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.14")
    implementation(project(":client"))
    implementation("redis.clients:jedis:6.1.0")
    implementation("io.lettuce:lettuce-core:6.8.0.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.10.2")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")

    implementation(libs.testcontainers.redis)
}

allOpen.annotation("org.openjdk.jmh.annotations.State")
benchmark.targets.register("main")
