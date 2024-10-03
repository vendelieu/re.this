<p align="center">
  <img src="./assets/logo.png" alt="Logo" />
</p>

# Re.This

[![Maven Central](https://img.shields.io/maven-central/v/eu.vendeli/re.this?style=flat&label=Maven&logo=apache-maven)](https://search.maven.org/artifact/eu.vendeli/re.this) [![CodeQL](https://github.com/vendelieu/re.this/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/vendelieu/re.this/actions/workflows/github-code-scanning/codeql)\
[![KDocs](https://img.shields.io/static/v1?label=Dokka&message=KDocs&color=blue&logo=kotlin)](https://vendelieu.github.io/re.this/)
[![codecov](https://codecov.io/gh/vendelieu/re.this/graph/badge.svg?token=F8SY97KR17)](https://codecov.io/gh/vendelieu/re.this)

# Overview

Re.This is a coroutine-based, multiplatform Redis client written in Kotlin.

It provides a simple and efficient way to interact with Redis from your Kotlin applications.

Built on raw sockets, using connection pool, it gives robust and fast interaction with Redis.

# Installation

To use Re.This in your project, add the following dependency to your Gradle build file:

```gradle
dependencies {
    implementation("eu.vendeli:re.this:0.1.0")
}
```

# Usage

### Connecting to Redis

To connect to a Redis instance, create a ReThis instance and specify the connection details:

```kotlin
val client = ReThis("localhost", 6379) // or just ReThis() if you're using default connection settings 
```

### Executing Commands

Re.This supports a comprehensive set of Redis commands, including:

* Data structures: Strings, Hash, Lists, Sets, Sorted Sets, Geospatial indexes
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
val result = client.execute(listOf("SET", "key", "value"))
```

# Documentation

You can learn more about the library through the following resources:

* [KDocs API Reference](https://vendelieu.github.io/re.this/)

# Targets

Re.This supports the following targets:

* JVM
* Linux (linuxArm64, linuxX64)
* Windows (mingwX64)
* IOS (iosArm64, iosSimulatorArm64, iosX64)
* MacOS (macosArm64, macosX64)
* TvOS (tvosArm64, tvosSimulatorArm64, tvosX64)
* WatchOS (watchosArm32, watchosArm64, watchosSimulatorArm64, watchosX64)

There are plans to add nodejs support.

# Compatibility

Re.This is compatible with:

- Java 11
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