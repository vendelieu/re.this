# Command Specifications

This directory contains declarative command specification interfaces that define the entire Redis API surface for the **re.this** client. Each spec is a `fun interface` annotated with `@RedisCommand` extending `RedisCommandSpec<T>`.

These specs are **not runtime code** — they are consumed at compile time by the **api-processor** (a KSP-based code generator) to produce codec objects and extension functions. For the full processing pipeline documentation, see the [api-processor README](../../../../../../../api-processor/README.md).

---

## Directory Structure

Commands are organized by Redis category, mirroring the official Redis command groups:

| Subdirectory   | Description                                |
|----------------|--------------------------------------------|
| `bitmap/`      | Bit-level operations (BITCOUNT, GETBIT, …) |
| `cluster/`     | Cluster management and introspection       |
| `connection/`  | AUTH, PING, CLIENT, HELLO, SELECT, …       |
| `generic/`     | Key-level operations (DEL, KEYS, SCAN, …)  |
| `geospatial/`  | Geospatial indices (GEOADD, GEOSEARCH, …)  |
| `hash/`        | Hash field operations (HSET, HGET, …)      |
| `hyperloglog/` | Probabilistic cardinality (PFADD, …)       |
| `json/`        | RedisJSON module commands                  |
| `list/`        | List operations (LPUSH, LRANGE, BLPOP, …)  |
| `pubsub/`      | Publish/Subscribe messaging                |
| `scripting/`   | Lua scripting and Functions API            |
| `sentinel/`    | Sentinel monitoring and failover           |
| `server/`      | Server management (ACL, FLUSHDB, INFO, …)  |
| `set/`         | Set operations (SADD, SINTER, SUNION, …)   |
| `sortedset/`   | Sorted set operations (ZADD, ZRANGE, …)    |
| `stream/`      | Stream operations (XADD, XREAD, …)         |
| `string/`      | String operations (GET, SET, INCR, …)      |
| `transaction/` | MULTI/EXEC transactions                    |

---

## Command Spec Anatomy

Every command spec follows the same pattern:

```kotlin
@RedisCommand("GET", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface GetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
    ): CommandRequest
}
```

**Key elements:**

- **`@RedisCommand`** — Marks the interface for KSP processing. Parameters:
  - `name` — The Redis command name (e.g. `"GET"`, `"SET"`)
  - `operation` — `RedisOperation.READ` or `RedisOperation.WRITE`
  - `responseTypes` — Array of expected `RespCode` values (`BULK`, `INTEGER`, `ARRAY`, `MAP`, `NULL`, …)
  - `isBlocking` — (optional, default `false`) Whether the command blocks the connection
- **`RedisCommandSpec<T>`** — The type parameter `T` determines the decoded response type
- **`suspend fun encode(...)`** — Defines the command's parameter signature; the api-processor generates encoder/decoder code from this

### Examples with advanced features

Command with options and `@RIgnoreSpecAbsence`:

```kotlin
@RedisCommand(
    "SET",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.SIMPLE_STRING, RespCode.NULL],
)
fun interface SetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        value: String,
        @RIgnoreSpecAbsence vararg options: SetOption,
    ): CommandRequest
}
```

Blocking command with `@RedisOption.Token`:

```kotlin
@RedisCommand(
    "XREAD",
    RedisOperation.READ,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
)
fun interface XReadCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(
        @RedisOption.Token("STREAMS") key: List<String>,
        id: List<String>,
        @RedisOption.Token("COUNT") count: Long?,
        @RedisOption.Token("BLOCK") milliseconds: Long?,
    ): CommandRequest
}
```

Command with a custom decoder:

```kotlin
@RedisCommand("SCAN", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = StringScanDecoder::class)
fun interface ScanCommand : RedisCommandSpec<ScanResult<String>> {
    suspend fun encode(
        cursor: Long,
        @RIgnoreSpecAbsence vararg option: ScanOption,
    ): CommandRequest
}
```

---

## Available Annotations

### Core

| Annotation                         | Target          | Purpose                                                                 |
|------------------------------------|-----------------|-------------------------------------------------------------------------|
| `@RedisCommand`                    | Spec interface  | Marks the interface for processing; carries command name, operation, response types, blocking flag |
| `@RIgnoreSpecAbsence`              | Parameter       | Suppresses "param not found in RSpec" validation for parameters absent from the official Redis spec |

### Meta (`@RedisMeta.*`)

| Annotation                         | Target          | Purpose                                                                 |
|------------------------------------|-----------------|-------------------------------------------------------------------------|
| `@RedisMeta.CustomCodec`           | Spec interface  | Override auto-generated encoder and/or decoder with custom implementations |
| `@RedisMeta.SkipCommand`           | Spec interface  | Generate codec only, skip the `ReThis` extension function               |
| `@RedisMeta.Default(value)`        | Parameter       | Set a default value for the parameter in the generated command function |
| `@RedisMeta.Weight(value)`         | Parameter       | Override the argument count contribution (default 1)                    |
| `@RedisMeta.WithSizeParam(name)`   | Parameter       | Emit an associated size parameter in the encoder                        |
| `@RedisMeta.OutgoingTimeUnit`      | Type            | Override inferred time unit for `Duration`/`Instant` parameters         |

### Options (`@RedisOption.*`)

| Annotation                         | Target               | Purpose                                                            |
|------------------------------------|----------------------|--------------------------------------------------------------------|
| `@RedisOption.Token("EX")`         | Sealed subclass/enum | Literal Redis token string sent in the wire protocol               |
| `@RedisOption.Name("addr")`        | Parameter            | Override Kotlin parameter name for spec path matching              |

---

## Naming Conventions

- **Standard commands**: `SetCommand` → generated `ReThis.set()`
- **ByteArray variants**: `SetBACommand` → generated `ReThis.setBA()`
- **Data objects** in sealed option hierarchies automatically emit their name as a Redis token. Override with `@RedisOption.Token("CUSTOM")`.

---

## How the API Processor Uses These Specs

The [api-processor](../../../../../../../api-processor/README.md) is a KSP-based code generator that transforms these specs through a multi-stage pipeline:

```text
@RedisCommand specs
       │
       ▼
Symbol Discovery (KSP finds all @RedisCommand interfaces)
       │
       ▼
Redis Spec Loading (commands.json, sentinel_spec.json, …)
       │
       ▼
Tree Construction (merge Kotlin AST + Redis spec into EnrichedTree)
       │
       ▼
Write Plan Generation (EnrichedTree → WriteOp sequence)
       │
       ▼
Code Generation (emit Kotlin via KotlinPoet)
```

### Generated output

For each command spec, the processor generates:

1. **Codec object** in `codecs/<category>/` — `encode()`, `encodeWithSlot()`, and `decode()` functions
2. **Extension function** in `command/<category>/` — ergonomic `ReThis.commandName(...)` API
3. **`RedisToken.kt`** in `utils/` — precomputed `ByteArray` constants for all `@RedisOption.Token` values

Generated files land in `client/build/generated/ksp/metadata/commonMain/kotlin`.

---

## Adding a New Command

1. **Create a file** in the appropriate category subdirectory (e.g. `string/MyCommand.kt`)
2. **Define the spec** following the standard pattern:
   ```kotlin
   @RedisCommand("MYCOMMAND", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
   fun interface MyCommandCommand : RedisCommandSpec<String> {
       suspend fun encode(
           key: String,
           value: String,
       ): CommandRequest
   }
   ```
3. **Run a clean build** — despite `ksp.incremental=false` in `gradle.properties`, always run `clean` when adding new specs to avoid stale generated artifacts
4. **Verify** that the codec and extension function were generated in `client/build/generated/ksp/`

Use `@RIgnoreSpecAbsence` for parameters not present in the official Redis spec (e.g. custom option types). Use `@RedisMeta.CustomCodec` if the response format requires non-standard decoding.

---

## Shared Types

The specs depend on types from the `shared` module:

- `CommandRequest` — Encoded command payload with operation type and blocking flag
- `RedisCommandSpec<T>` — Base interface for all command specs
- `RedisOperation` — `READ` or `WRITE`
- `RespCode` — RESP protocol response codes (`BULK`, `INTEGER`, `ARRAY`, `MAP`, `NULL`, `SIMPLE_STRING`, …)
- Request types (e.g. `SetOption`, `ScanOption`) in `shared/request/`
- Response types (e.g. `ScanResult`, `GeoPosition`) in `shared/response/`

---

## Further Reading

- [api-processor README](../../../../../../../api-processor/README.md) — Full documentation of the KSP processing pipeline, enriched tree construction, write plan generation, codec/command generation, and all configuration details
