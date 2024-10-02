<p align="center">
  <img src="./assets/logo.png" alt="Logo" />
</p>

# Re.This

[![Maven Central](https://img.shields.io/maven-central/v/eu.vendeli/re.this?style=flat&label=Maven&logo=apache-maven)](https://search.maven.org/artifact/eu.vendeli/re.this)\
[![KDocs](https://img.shields.io/static/v1?label=Dokka&message=KDocs&color=blue&logo=kotlin)](https://vendelieu.github.io/re.this/)
[![codecov](https://codecov.io/gh/vendelieu/re.this/branch/master/graph/badge.svg?token=xn5xo6fu6r)](https://codecov.io/gh/vendelieu/re.this)

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

Also you can execute Redis commands using the execute method:

```kotlin
val result = client.execute("SET", "key", "value")
```

# Targets

Re.This supports the following targets:

* jvm
* iosArm64
* iosSimulatorArm64
* iosX64
* linuxArm64
* linuxX64
* macosArm64
* macosX64
* mingwX64
* tvosArm64
* tvosSimulatorArm64
* tvosX64
* watchosArm32
* watchosArm64
* watchosSimulatorArm64
* watchosX64

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