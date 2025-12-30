# Re.this Changelog

## 0.3.9

* Fixed correct encoding of `Instant` values to seconds (#98).
* Changed `ByteString` serialization format to `ByteArray` for more flexibility.
* Changed `reDistributedLock` to wire to one coroutine Job, moved old implementation to `reHierarchicalDistributedLock`.

## 0.3.8

* Added `ByteString` support, implemented for `GET`, `SET`, `GETDEL`, `GETEX`, `GETRANGE`, `HGET`, `HGETALL`,
  `HRANDFIELD`, `LPOP`, `RPUSH`, `LMOVE`, `BLMOVE`, `SPOP`, `SRANDMEMBER`, `ZADD`, `ZRANDMEMBER` commands.

## 0.3.7

* Fixed freezes on native targets (#93).
* Improved error handling in RType wrappers.
* Bumped dependency versions.

## 0.3.6

* Added experimental distributed lock, try with `client.reDistributedLock`.
* Added logging entries when response is substituted with default value in specific modes (transaction, pipeline).

## 0.3.5

* Fixed ConcurrentModificationException in subscriptions (thanks to @peterdk).
* Improved pubsub CancelationException handling (thanks to @peterdk).
* Improved using transactions in subscriptions.

## 0.3.4

* Fixed decoder of `REPLICAOF` and `SHUTDOWN` commands.

## 0.3.3

* Returned serialization support, with ability to use different serialization formats \
  (through configuration `serializationFormat` parameter, or by command function parameter), \
  supported commands `get`, `hget`, `hmget`, `hset`, `hvals`, `json.get`, `json.mget`, `json.set`, `mget`, `mset`,
  `set`.

## 0.3.2

* Improve logging
* Fix pubsub event handling

## 0.3.1

* Return missing socket configuration.

## 0.3.0

* Refactored project structure, some imports may break.
* Added experimental support for cluster and sentinel.

## 0.2.9

* Reorganized project structure (some imports may break).
* Moved more tools for work with `RType` into user space.
* Covered `nodejs` and `wasmjs` targets.

## 0.2.8

* Add experimental serialization support.

## 0.2.7

* Try to fix jvm package.

## 0.2.6

* Added `RType.unwrap` function to unwrap RType.
* Improved List nullable responses parsing.
* Changed artifactId from `re.this` to `rethis` to avoid building problems at some platforms.

## 0.2.5

* Added `shutdown` hook.
* Improved request flow.

## 0.2.4

* Enhanced the algorithm for parsing.

## 0.2.3

* Reorganized response parsing to improve performance, improved benchmarks.

## 0.2.2

* Fixed casting issue caused by type erasure and wrong inlining.

## 0.2.1

* Added connection retrying.
* Added socket configuration.
* Upgraded JVM target version to 17

## 0.2.0

* Implemented Bitmap, Stream commands.

## 0.1.9

* Improved redis response parsing.

## 0.1.8

* Improved pub/sub flow, added common event handler.

## 0.1.7

* Supported android targets (androidNativeArm32, androidNativeArm64, androidNativeX64, androidNativeX86).

## 0.1.6

* Added tls connection support

## 0.1.5

* Added option to pass redis url address.

## 0.1.4

* Use stricter typing in arguments.
* Removed unnecessary option constructs and moved to `VaryingArgument`.
* Removed unnecessary different type write operations since
  the [protocol states](https://redis.io/docs/latest/develop/reference/protocol-spec/#resp-protocol-description)
  that it always array of bulk strings.
* Supported setting charset for incoming and outgoing messages.

## 0.1.3

* Publish missing meta package

## 0.1.2

* Fixing build

## 0.1.1

* Published to maven

## 0.1.0

* Initial version