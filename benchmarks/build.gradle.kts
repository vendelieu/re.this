plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") version "2.3.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.16"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.16")
    implementation(project(":client"))
    implementation("redis.clients:jedis:7.3.0")
    implementation("io.lettuce:lettuce-core:7.4.0.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.10.2")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")

    implementation(libs.testcontainers.redis)
}

allOpen.annotation("org.openjdk.jmh.annotations.State")
benchmark.targets.register("main")

tasks.register("copyLatestBenchmarkJson") {
    group = "benchmark"
    description = "Copies the latest benchmark JSON report to the project root as report.json"

    doLast {
        val benchmarkDir = file("build/reports/benchmarks")
        if (!benchmarkDir.exists()) {
            println("No benchmark reports found in $benchmarkDir")
            return@doLast
        }

        // Find all JSON files recursively
        val jsonFiles = benchmarkDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .toList()

        if (jsonFiles.isEmpty()) {
            println("No JSON files found in $benchmarkDir")
            return@doLast
        }

        // Pick the latest file by last modified time
        val latestJson = jsonFiles.maxBy { it.lastModified() }

        // Copy to project root as report.json
        val targetFile = file("report.json")
        latestJson.copyTo(targetFile, overwrite = true)

        println("Copied latest benchmark JSON: ${latestJson.name} → ${targetFile.path}")
    }
}
