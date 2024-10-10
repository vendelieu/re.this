plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") version "2.0.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.11"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.11")
    implementation(project(":"))
    implementation("redis.clients:jedis:5.2.0")
    implementation("io.lettuce:lettuce-core:6.4.0.RELEASE")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
}

allOpen.annotation("org.openjdk.jmh.annotations.State")

benchmark {
    targets.register("main")
}
