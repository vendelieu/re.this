<p align="center">
  <img src="./assets/logo.png" alt="Logo" />
</p>

# <img src="./assets/logo-icon.svg" alt="icon" height="30" /> re.this

[![Maven Central](https://img.shields.io/maven-central/v/eu.vendeli/re.this?style=flat&label=Maven&logo=apache-maven)](https://search.maven.org/artifact/eu.vendeli/re.this) [![CodeQL](https://github.com/vendelieu/re.this/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/vendelieu/re.this/actions/workflows/github-code-scanning/codeql)\
[![KDocs](https://img.shields.io/static/v1?label=Dokka&message=KDocs&color=blue&logo=kotlin)](https://vendelieu.github.io/re.this/)
[![codecov](https://codecov.io/gh/vendelieu/re.this/graph/badge.svg?token=F8SY97KR17)](https://codecov.io/gh/vendelieu/re.this)

[![Validate Gradle Wrapper](https://github.com/vendelieu/re.this/actions/workflows/gradle-wrapper-validation.yml/badge.svg)](https://github.com/vendelieu/re.this/actions/workflows/gradle-wrapper-validation.yml)

# Overview

Re.This is a coroutine-based, multiplatform Redis client written in Kotlin.

It provides a simple and efficient way to interact with Redis from your Kotlin applications.

Built on raw sockets, using connection pool, it gives robust and fast interaction with Redis.

# Installation

To use Re.This in your project, add the following dependency to your Gradle build file:

```gradle
dependencies {
    implementation("eu.vendeli:rethis:0.2.5")
}
```

# Benchmark

There is
a [benchmark](https://github.com/vendelieu/re.this/tree/master/benchmarks/src/main/kotlin/eu/vendeli/rethis/benchmarks)
comparing popular library solutions (more is better):

```javascript
Benchmark                        Mode  Cnt        Score        Error  Units
JedisBenchmark.jedisSetGet      thrpt    5    31393.802 ±   5501.760  ops/s
KredsBenchmark.kredsSetGet      thrpt    5  1051938.506 ± 344626.183  ops/s
LettuceBenchmark.lettuceSetGet  thrpt    5    19780.976 ±   1544.416  ops/s
RethisBenchmark.rethisSetGet    thrpt    5  1555443.251 ± 552464.716  ops/s
```

<details>
  <summary>Details</summary>

* `Jedis` (Pooled) gives roughly the same results inside the coroutine as outside.
* `Kreds` with the `.use {}` approach gives worse results.
* The most interesting thing happens with `Lettuce` it gives excellent results (almost as `Re.This`, sometimes really
  near)
  if you use its asynchronous client on top of coroutines, but in this case the Heap dies in seconds (goes out of memory).
  And if swap gc (but still using coroutines) to zgc it gives same performance as `kreds` but now without the memory
  problems.

</details>


<details>
  <summary>Specs</summary>
  Intel® Core™ i9-10900K CPU @ 3.70GHz × 20 | RAM: 16,0 GiB
</details>

# Usage

### Connecting to Redis

To connect to a Redis instance, create a ReThis instance and specify the connection details:

```kotlin
val client = ReThis("localhost", 6379) // or just ReThis() if you're using default connection settings 
```

### Executing Commands

Re.This supports a comprehensive set of Redis commands, including:

* Data structures: Strings, Hash, Lists, Sets, Sorted Sets, Geospatial indexes, Streams, Bitmaps
* Advanced features: Blocking commands, Pipelining, Publish/Subscribe, Connection handling commands
* Additional features: RedisJSON support, Scripting support, Functions, Transactions

to use them, you can call method by its name:

```kotlin
client.set("testKey", "testValue")
client.get("testKey") // == testValue
```

#### Subscription

Using pub/sub in the library is quite easy, call the `subscribe`/... function,
and the library will take care of the processing.

```kotlin
client.subscribe("testChannel") { client, message ->
    println(message)
}
```

To manage the lifecycle of subscriptions, you can use the `client.subscriptions` parameter.

#### Pipelining

Pipelining can also be easily achieved with an appropriate scope function:

```kotlin
client.pipeline {
    set("key", "value")
    get("key")
}
```

Executing the scope returns the result of the pipelines so the result will not go missing anywhere :)

#### Transactions

As you might have realized for transactions there is also a similar DSL that can help in your development.

```kotlin
client.transaction {
    set("key", "value")
    get("key")
}
```

transaction functionality also takes into account fail-state cases and gracefully completes transactions.

(yes, if you have also thought about mixing pipelines and transactions, we have taken such a case into account ;) )

#### More out-of-the-box stuff/commands.

Also you can execute Redis commands using the execute method:

```kotlin
val result = client.execute(listOf("SET", "key", "value").toArg())
```

# Resources/Documentation

You can learn more about the library through the following resources:

* [KDocs API Reference](https://vendelieu.github.io/re.this/)
* [Github Discussion](https://github.com/vendelieu/re.this/discussions)

# Targets

Re.This supports the following targets:

* JVM
* Linux (linuxArm64, linuxX64)
* Windows (mingwX64)
* Android (androidNativeArm32, androidNativeArm64, androidNativeX64, androidNativeX86)
* IOS (iosArm64, iosSimulatorArm64, iosX64)
* MacOS (macosArm64, macosX64)
* TvOS (tvosArm64, tvosSimulatorArm64, tvosX64)
* WatchOS (watchosArm32, watchosArm64, watchosSimulatorArm64, watchosX64)

There are plans to add nodejs support.

# Compatibility

Re.This is compatible with:

- Java 17
- Redis (any version)

Supports and handles the use of `RESPv2`/`RESPv3` and handles differences in responses within a command.

The only note is that deprecated commands were omitted during development (you can use `execute` method).

# Contribution

We'd love your contributions!

* Bug reports are always welcome! You can open a bug report on GitHub.
* You can also contribute documentation or anything to improve Re.This.

Please see our contribution [guideline](./CONTRIBUTING.md) for more details.

# Acknowledgements

A big thank you to everyone who has contributed to this project. Your support and feedback are invaluable.

If you find this library useful, please consider giving it a star. Your support helps us continue to improve and
maintain this project.
