plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") version "2.1.21"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.14")
    implementation(project(":"))
    implementation("redis.clients:jedis:6.0.0")
    implementation("io.lettuce:lettuce-core:6.6.0.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.10.2")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")

    implementation("com.redis:testcontainers-redis:1.7.0") {
        exclude("commons-io", "commons-io")
        exclude("org.apache.commons", "commons-compress")
        exclude("com.fasterxml.woodstox", "woodstox-core")
    }
    implementation("commons-io:commons-io:2.19.0")
    implementation("org.apache.commons:commons-compress:1.27.1")
    implementation("com.fasterxml.woodstox:woodstox-core:7.1.0")
}

allOpen.annotation("org.openjdk.jmh.annotations.State")
benchmark.targets.register("main")
