<p align="center">
  <img src="./assets/logo.png" alt="Re.This Logo" width="200" />
</p>

[![Maven Central](https://img.shields.io/maven-central/v/eu.vendeli/rethis?style=flat&label=Maven&logo=apache-maven)](https://search.maven.org/artifact/eu.vendeli/rethis) [![KDocs](https://img.shields.io/static/v1?label=Dokka&message=KDocs&color=blue&logo=kotlin)](https://vendelieu.github.io/re.this/) \
[![codecov](https://codecov.io/gh/vendelieu/re.this/graph/badge.svg?token=F8SY97KR17)](https://codecov.io/gh/vendelieu/re.this)

## What is Re.This?

Re.This is a **Kotlin Multiplatform** Redis client built for **coroutine‑based**, **non‑blocking**, **high‑performance**
applications.

* ✅ **Raw sockets** & connection pooling for rock‑solid throughput
* ✅ Full support for RESPv2/RESPv3, RedisJSON, Streams, Pub/Sub, and more
* ✅ Tiny footprint over all targets: JVM, Android, iOS, WASM, Node.js, and other native targets
* ✅ Сompatible with Valkey/KeyDB — since it shares the same API as Redis.

Designed for modern Kotlin developers seeking a **fast**, **lightweight**, and **idiomatic** Redis integration.

## Key Features

* **Complete Command Coverage**
  Strings, Hashes, Lists, Sets, Sorted Sets, Streams, Bitmaps, Geospatial, Transactions, Scripting, Functions, …
* **Advanced Patterns**
  * Pipelining & Transactions DSL
  * Publish/Subscribe with lifecycle management
  * Script & JSON support
* **Multiplatform Targets**
  JVM, Android, iOS, Linux, Windows, macOS, tvOS, watchOS, Node.js, WASM
* **High Performance**
  Optimized for throughput and minimal latency via raw socket I/O

## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("eu.vendeli:rethis:0.4.0")
}
```

## Quick Start

```kotlin
suspend fun main() {
    // 1. Initialize client (defaults to localhost:6379)
    val client = ReThis()

    // 2. Simple SET/GET
    client.set("hello", "world")
    println(client.get("hello")) // → "world"

    // 3. Pipeline multiple commands
    val results = client.pipeline {
        set("foo", "bar")
        get("foo")
    }
    println(results) // → [OK, "bar"]
  
    // 4. Transaction
    val txResults = client.transaction {
        set("bar", "baz")
        get("bar")
    }
    println(txResults) // → [OK, "baz"]

    // 5. Pub/Sub example
    client.subscribe("news") { _, msg -> println("Received: $msg") }
}
```

## Benchmarks & Performance

<!-- BENCHMARK_TABLE -->
| Library        |       Ops/sec |
|----------------|--------------:|
| Jedis (pooled) | **2,038** |
| Kreds | **448** |
| Lettuce | **1,747** |
| Rethis | **2,171** |
<!-- END_BENCHMARK_TABLE -->

See more detailed [report](benchmarks/report.json).

## Compatibility & Requirements

* **Java**: 17+
* **Redis/Valkey/KeyDB**: any modern version (RESPv2/v3)

## Resources & Documentation

* 📖 [KDocs API Reference](https://vendelieu.github.io/re.this/)
* 💬 [GitHub Discussions](https://github.com/vendelieu/re.this/discussions)
* 🐞 [Issue Tracker](https://github.com/vendelieu/re.this/issues)

## Acknowledgements

Thanks to all contributors and users—your feedback drives our improvements!

⭐ If you find Re.This helpful, please give it a star on GitHub!
