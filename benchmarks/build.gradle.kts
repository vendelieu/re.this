import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.util.*

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") version "2.3.21"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.16"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.16")
    implementation(project(":client"))
    implementation("redis.clients:jedis:7.5.0")
    implementation("io.lettuce:lettuce-core:7.5.1.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.10.2")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")

    implementation(libs.testcontainers.redis)
}

allOpen.annotation("org.openjdk.jmh.annotations.State")
benchmark.targets.register("main")

tasks.register("updateBenchmarkTableInReadme") {
    group = "benchmark"
    description = "Updates the benchmark table in README.md between markers from latest JSON"

    doLast {
        val benchmarkDir = file("build/reports/benchmarks")
        if (!benchmarkDir.exists()) {
            println("No benchmark output directory found: $benchmarkDir")
            return@doLast
        }

        // Pick the latest JSON
        val latestJson = benchmarkDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .maxByOrNull { it.lastModified() } ?: run {
            println("No JSON files found in $benchmarkDir")
            return@doLast
        }

        val benchmarksArray = Json.parseToJsonElement(latestJson.readText()).jsonArray

        // Map library names → Ops/sec (formatted with commas)
        val resultsMap = mutableMapOf<String, String>()
        benchmarksArray.forEach { bench ->
            val obj = bench.jsonObject
            val fqName = obj["benchmark"]?.jsonPrimitive?.content ?: return@forEach
            val score = obj["primaryMetric"]?.jsonObject?.get("score")?.jsonPrimitive?.double ?: return@forEach
            val formatted = NumberFormat.getNumberInstance(Locale.US).format(score.toLong())

            when {
                fqName.contains("RethisBenchmark") -> resultsMap["Rethis"] = formatted
                fqName.contains("LettuceBenchmark") -> resultsMap["Lettuce"] = formatted
                fqName.contains("KredsBenchmark") -> resultsMap["Kreds"] = formatted
                fqName.contains("JedisBenchmark") -> resultsMap["Jedis (pooled)"] = formatted
            }
        }

        if (resultsMap.isEmpty()) {
            println("No recognized benchmarks found in JSON")
            return@doLast
        }

        // Generate Markdown table
        val tableMarkdown = buildString {
            appendLine("| Library        |       Ops/sec |")
            appendLine("|----------------|--------------:|")
            resultsMap.forEach { (lib, ops) ->
                appendLine("| $lib | **$ops** |")
            }
        }

        // Read README and replace content between markers
        val readmeFile = file("../README.md")
        val readmeText = readmeFile.readText()

        val newReadme = readmeText.replace(
            Regex("(?s)(<!-- BENCHMARK_TABLE -->).*?(<!-- END_BENCHMARK_TABLE -->)"),
            "$1\n$tableMarkdown$2",
        )

        readmeFile.writeText(newReadme)
        println("README.md benchmark table updated successfully:\n$resultsMap")
    }
}
