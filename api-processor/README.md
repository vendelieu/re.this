# API Processor

A Kotlin Symbol Processing (KSP) based code generator for the re.this Redis client library. This processor automatically generates type-safe codec classes and extension functions for Redis commands based on declarative command specifications.

## Overview

The API Processor transforms annotated interface specifications (in the `api-spec` module) into:

1. **Codec Objects** – Handle encoding commands to RESP protocol and decoding responses  
2. **Extension Functions** – Provide ergonomic APIs on the `ReThis` client for each command

This eliminates boilerplate code and ensures consistency between command specifications and their implementations.

---

## Architecture

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                              api-spec                                    │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  @RedisCommand("SET", RedisOperation.WRITE, [RespCode.BULK])     │   │
│  │  fun interface SetCommand : RedisCommandSpec<String> {           │   │
│  │      suspend fun encode(key: String, value: String): ...         │   │
│  │  }                                                                │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ KSP Processing
┌─────────────────────────────────────────────────────────────────────────┐
│                           api-processor                                  │
│  ┌────────────────┐  ┌──────────────┐  ┌────────────────────────────┐   │
│  │ RedisCommand   │  │ LibTree      │  │ RedisSpec                  │   │
│  │ Processor      │─▶│ Planter      │─▶│ Loader                     │   │
│  │                │  │              │  │ (Redis official JSON spec) │   │
│  └────────────────┘  └──────────────┘  └────────────────────────────┘   │
│           │                  │                      │                    │
│           ▼                  ▼                      ▼                    │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │               EnrichedTree (merged Kotlin + Redis spec)        │     │
│  └────────────────────────────────────────────────────────────────┘     │
│           │                                                              │
│           ▼                                                              │
│  ┌────────────────┐  ┌────────────────┐                                 │
│  │ Encoder        │  │ Decoder        │                                 │
│  │ Generation     │  │ Generation     │                                 │
│  └────────────────┘  └────────────────┘                                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ Generated Output
┌─────────────────────────────────────────────────────────────────────────┐
│                              client                                      │
│  ┌──────────────────────────────┐  ┌─────────────────────────────────┐  │
│  │ codecs/string/               │  │ command/string/                 │  │
│  │   SetCommandCodec.kt         │  │   Set.kt                        │  │
│  │   - encode(charset, ...)     │  │   - ReThis.set(key, value, ...) │  │
│  │   - encodeWithSlot(...)      │  │                                 │  │
│  │   - decode(input, charset)   │  │                                 │  │
│  └──────────────────────────────┘  └─────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
````

---

## Processing Pipeline

### 1. Symbol Discovery

The processor scans for all interfaces annotated with `@RedisCommand`:

```kotlin
@RedisCommand(
    name = "SET",
    operation = RedisOperation.WRITE,
    responseTypes = [
        RespCode.BULK,
        RespCode.SIMPLE_STRING,
        RespCode.NULL
    ],
    isBlocking = false
)
fun interface SetCommand : RedisCommandSpec<String> { ... }
```

---

### 2. Redis Specification Loading

The processor loads official Redis command specifications from:

* `commands.json` – Core Redis commands
* `commands_redisjson.json` – RedisJSON module commands
* `sentinel_spec.json` – Sentinel commands (bundled locally)
* `resp2_replies.json` / `resp3_replies.json` – Response type mappings

These specs provide:

* Argument structure and ordering
* Optional / required flags
* Token names (`EX`, `PX`, `NX`, …)
* Key indices

---

### 3. Tree Construction (LibTreePlanter)

The processor builds an **Enriched Tree** that merges:

* **Kotlin AST** – Parameter types, annotations, nullability
* **Redis Spec** – Argument names, tokens, optionality, key positions

```text
EnrichedNode (root: encode function)
├── EnrichedNode (param: key)
│   ├── Attr: Name("key")
│   ├── Attr: Type(String)
│   ├── Attr: Key
│   └── Attr: RelatedRSpec
├── EnrichedNode (param: value)
│   ├── Attr: Name("value")
│   └── Attr: Type(String)
└── EnrichedNode (param: options)
    ├── Attr: Multiple(vararg=true)
    ├── Attr: Optional
    └── children:
        └── EnrichedNode (sealed class: SetOption)
            ├── SetExpire.Ex  → Token("EX")
            ├── SetExpire.Px  → Token("PX")
            └── ...
```

---

### 4. Write Plan Generation

The enriched tree is transformed into a sequence of `WriteOp`s:

* **DirectCall** – Simple value writes
* **WrappedCall** – Nullability, varargs, collections
* **Dispatch** – `when` expressions for sealed hierarchies

---

### 5. Code Generation

#### Encoder Generation

```kotlin
public object SetCommandCodec {
    private val COMMAND_HEADER = Buffer().apply {
        writeString("\r\n$3\r\nSET\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        value: String,
        vararg options: SetOption
    ): CommandRequest {
        // ...
    }

    public suspend fun encodeWithSlot(...): CommandRequest {
        // Redis Cluster slot calculation
    }
}
```

#### Decoder Generation

```kotlin
public suspend fun decode(input: Buffer, charset: Charset): String? {
    val code = input.parseCode(RespCode.BULK)
    return when (code) {
        RespCode.BULK -> BulkStringDecoder.decodeNullable(input, charset, code)
        RespCode.SIMPLE_STRING -> SimpleStringDecoder.decode(input, charset, code)
        RespCode.NULL -> null
        else -> throw UnexpectedResponseType(code)
    }
}
```

#### Command Function Generation

```kotlin
public suspend fun ReThis.set(
    key: String,
    value: String,
    vararg options: SetOption
): String? {
    val request =
        if (cfg.withSlots)
            SetCommandCodec.encodeWithSlot(cfg.charset, key, value, options)
        else
            SetCommandCodec.encode(cfg.charset, key, value, options)

    return SetCommandCodec.decode(topology.handle(request), cfg.charset)
}
```

---

## Configuration

```kotlin
ksp {
    arg(
        "clientProjectDir",
        rootDir.resolve("client/src/commonMain/kotlin").absolutePath
    )
}
```

Yes, here are some specific details that new developers should know:

## Additional Details

### 1. **Naming Convention for BS (ByteString) Commands**

Commands that work with `ByteString` instead of `String` follow a naming pattern:
- Spec class: `SetBSCommand`, `GetBSCommand`
- Generated function: `setBS()`, `getBS()`

The `BS` suffix is automatically handled - the function name is derived from the codec name by removing `CommandCodec` and lowercasing the first letter.

### 2. **Redis Spec Fetching Happens at Compile Time**

`RedisSpecLoader` fetches specs from GitHub **during KSP processing** (not at runtime). This means:
- Build will fail if network is unavailable and specs aren't cached
- The URL is pinned to a specific commit hash for stability
- Sentinel specs are bundled locally in `resources/sentinel_spec.json` because they're not in the official repo

### 3. **The `RSpecNode.path` Matching System**

Redis specs have nested argument structures. The processor uses path-based matching to find the correct spec node:
- Each spec node has a `path` (e.g., `["SET", "condition", "NX"]`)
- Parameters are matched by their "normalized name" against spec paths
- The `isWithinBounds()` function checks if a node's path is within the parent's bounds

### 4. **Slot Calculation for Redis Cluster**

Keys are identified by `keySpecIndex` in the Redis spec. The processor:
- Generates `encodeWithSlot()` that calculates CRC16 hash
- Uses `validateSlot()` to ensure all keys hash to the same slot
- Throws `KeyAbsentException` if a collection key parameter is empty

### 5. **The `haveVaryingSize` Flag**

Commands with optional/vararg parameters can't know the argument count at compile time. When `haveVaryingSize` is true:
- The codec uses `var size = X` instead of a constant
- Each optional argument increments `size += 1`
- The final RESP array header `*{size}` is prepended at the end

### 6. **Context is Static (Singleton)**

```kotlin
internal companion object {
    val context = ProcessorContext()
}
```


The `ProcessorContext` is a **static singleton**. This is why `clearPerCommand()` and `clearAll()` exist - to reset state between processing different commands and between builds.

### 7. **`@RIgnoreSpecAbsence` Annotation**

When you add a parameter that doesn't exist in the official Redis spec (like custom options), use this annotation to suppress the "Param not found in RSpec" warning.

### 8. **Custom Codec Support**

For commands with complex response parsing, you can bypass auto-generation:
```kotlin
@RedisMeta.CustomCodec(decoder = MyDecoder::class, encoder = MyEncoder::class)
```


Check for this with `hasCustomEncoder` / `hasCustomDecoder` properties on `CurrentCommand`.

### 9. **Token vs Name vs DisplayText**

In Redis specs:
- `token` - The literal string sent in the command (e.g., "EX", "NX")
- `name` - Internal identifier used for matching
- `displayText` - Human-readable name (not used by processor)

The processor matches Kotlin parameter names against `name` (normalized to camelCase).

### 10. **Data Objects Generate Tokens Automatically**

Kotlin `data object` declarations automatically emit their name as a token:
```kotlin
data object GET : SetOption  // Emits "GET" token
```


The token name can be overridden with `@RedisOption.Token("CUSTOM")`.

### 11. **TimeUnit Handling**

Duration/Instant parameters need time unit conversion. The spec's expected unit is inferred from the token name:
- `EX` → seconds
- `PX` → milliseconds
- `EXAT`/`PXAT` → Unix timestamps

Use `@RedisMeta.OutgoingTimeUnit` to override.

### 12. **Decoder Selection Logic**

Response decoding uses this priority:
1. Check for `@RedisMeta.CustomCodec`
2. Check if return type is `ByteString` → use `BulkByteStringDecoder`
3. Match against `plainDecoders`, `collectionDecoders`, or `mapDecoders` maps
4. Generate `when` block for multiple possible response types

### 13. **The `WriteOp` Hierarchy**

Understanding how code emission works:
- `DirectCall` - Leaf operation, writes a single value
- `WrappedCall` - Adds guards (`?.let`, `forEach`, `if (isNotEmpty())`)
- `Dispatch` - Generates `when(x) { is Type -> ... }` for sealed classes

Props like `NULLABLE`, `COLLECTION`, `SINGLE_TOKEN` control wrapping behavior.

### 14. **KSP Incremental Processing Caveats**

Despite `ksp.incremental=false` in gradle.properties, you may still encounter issues. Always run `clean` when:
- Adding new command specs
- Modifying shared types used by multiple specs
- Changing the processor itself