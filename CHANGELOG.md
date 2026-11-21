# Re.this Changelog

## Unreleased
* Fixed the connection pool timing out the first `acquire()` when `maxConnections` is reached by the prepopulated connections (for example `maxConnections = 1` with the default `minIdleConnections`): a prepopulated connection is now handed to a waiting caller before it is parked as idle.
* Added `ReThis.withConnection(key) { ... }`, a pinned-connection scope for `WATCH` / `UNWATCH` / `MULTI` / `EXEC`: every command in the block runs on one connection (on a cluster, the master owning the slot of `key`), `UNWATCH` is sent when the block completes and the connection is returned to the pool (or discarded if the block fails or is cancelled). `WATCH` through the plain pooled client still logs a warning, which now points to it.
* **Behaviour change: `unwrap<String>()` and `unwrapList<String>()` of a RESP3 verbatim string now return the bare content** (`Some string`) instead of `txt:Some string`, so they agree with the typed decoders. `VerbatimString.value` still holds `encoding:data`; the format is available as `VerbatimString.encoding` and the content as `VerbatimString.data`.
* **Behaviour change: `commandTimeout` now defaults to 30 seconds instead of `null` (unbounded).** A server that stops replying now fails the command with `CommandTimeoutException` (and, for reads, a retry) instead of hanging the caller forever. Set `commandTimeout = null` to keep the old unbounded behaviour. Blocking commands (`BLPOP`, `BRPOP`, `BLMOVE`, `BLMPOP`, `BZPOP*`, `BZMPOP`, `XREAD`, `XREADGROUP`, `WAIT`, `WAITAOF`) are exempt from `commandTimeout`, so a long server-side timeout is no longer cut off (this also applies when `commandTimeout` is set explicitly); pipelines containing a blocking command are exempt as a whole. `XREAD` and `XREADGROUP` are flagged blocking whether or not `BLOCK` is given, so they have no default timeout either.
* `ReThis("rediss://...")` is now accepted and connects over TLS with the default `TLSConfig`; it otherwise parses like `redis://` (credentials, port, db). An explicit `tls { }` block in the configurator takes precedence. When the TLS config has no `serverName`, the host being connected to is used for SNI and certificate host verification (per connection, so cluster and Sentinel nodes are verified against their own host); an explicit `serverName` wins. Cluster and Sentinel clients take `Address` lists rather than URLs and are configured through `tls { }` as before.
* Failed connection attempts while pre-warming the pool (`pool.minIdleConnections`) are now logged at `WARN` instead of `DEBUG`.
* **Behaviour change: `ReThis("redis://user:password@host")` now authenticates with the right credentials.** Previously the URL user was sent as the password and the URL password as the username. The password-only form `redis://:password@host` now sends `AUTH password` (no username), a user-info without a colon (`redis://password@host`) is still read as a password, `%xx` escapes in the user and password are decoded (a literal `%` must now be written as `%25`), and IPv6 hosts in brackets (`redis://[::1]:6379`) are accepted. Callers that worked around the swap by putting the password in the username position must switch to the standard form.
* **Behaviour change: `ReThis(address: String)` now throws `IllegalArgumentException` for an invalid URL instead of silently connecting to `localhost:6379` without credentials.** This covers any URL that is not `redis://[[user]:password@]host[:port][/db]`: a `rediss://` (or other) scheme, a password containing a raw `@` (it must be written as `%40`), a non-numeric or out-of-range port (1..65535) or db, and an unparsable host. The exception message never contains the URL, since it can carry credentials. URL query keys and values are now split on the first `=` and percent-decoded, and empty query entries are ignored.
* `ReThisMap.clear()` now deletes the hash key atomically with `DEL`, and `ReThisMap.putAll` / `ReThisSet.addAll` with an empty input are no-ops instead of failing with a server error.
* `ReThisSet` iterator removal now writes through to Redis, so `Iterator.remove()`, `removeAll` and `retainAll` modify the stored set instead of only a local copy. Calling `remove()` before `next()` or twice throws `IllegalStateException`.
* `FUNCTION FLUSH` mode is now optional: `functionFlush` accepts a nullable `FlushType` (like `SCRIPT FLUSH`) so the server default can be used.
* `JSON.ARRINDEX` can now be served by replicas (it is declared as a read).
* `HPERSIST` is now routed as a write (master) instead of a read.
* Fixed `XReadGroupDecoder` swallowing protocol errors: a corrupt header or a malformed stream or message entry now throws `ResponseParsingException` instead of returning an empty or truncated result, and a RESP3 null reply (for example an `XREADGROUP BLOCK` timeout) is now reported as `null` by `decodeNullable` instead of an empty list.
* Fixed `LPopDecoder`, `LMPopDecoder` and the `SCAN`/`HSCAN` decoders failing with raw Kotlin exceptions: a null reply (`BLMPOP` timeout) now yields an empty result (`BLPOP`/`BRPOP` now return `null`, see Breaking changes), malformed or short replies (including an odd-length `HSCAN` array) throw `ResponseParsingException`, and an already-consumed type byte is honoured.
* Fixed `ZMemberBA.equals` disagreeing with `hashCode` for `0.0` and `-0.0` scores (they are now distinct) and `NaN` scores never equalling themselves (they now do).

* Fixed `CLUSTERDOWN` mapping: the typed exceptions are now produced for the messages Redis actually sends (`CLUSTERDOWN Hash slot not served` -> `DownUnboundSlotException`, `CLUSTERDOWN The cluster is down and only accepts read commands` -> `DownReadOnlyStateException`); previously they matched `-UNBOUND`/`-READONLY` tokens that never occur and fell through to a generic `ReThisException`.
* Fixed `MOVED`/`ASK` redirect parsing: host and port are split on the last colon so IPv6 redirect targets (`MOVED 3999 ::1:6381`) are followed, and malformed redirect lines now produce a plain `ReThisException` instead of `NumberFormatException`/`IndexOutOfBoundsException`.
* Fixed cause inference for RESP3 bulk errors: the length header and trailing CRLF are no longer fed into the prefix matching, so `MOVED`, `ASK`, `CROSSSLOT` and the other cluster errors arriving as bulk errors map to their typed exceptions, and unrecognised payloads produce a `ReThisException` carrying just the server message.
* Fixed `BulkErrorDecoder` failing with `IllegalArgumentException: No suitable message type found` on a RESP3 bulk error (`!<len>\r\n<payload>\r\n`): the length header is now read as a byte count and the payload is thrown as a `ReThisException` carrying the server message.
* Fixed cluster and sentinel topology refresh dying after the first failed refresh (for example every seed unreachable at startup): failures are now logged and the refresh scope and periodic loop stay alive, and the cluster client fetches the topology on demand when a command arrives before any topology is known (concurrent commands share one fetch, and a failed fetch is not retried for one second).
* Fixed the cluster topology fetch consuming its seed list, which made retries against a single seed fail with `NoSuchElementException`; seeds are now cycled across attempts.
* Fixed cluster `ASK` redirects being sent back to the slot's current owner instead of the node named in the error: the request now goes to that node (a temporary connection provider is used and closed when the address is not in the topology), the cluster topology is left unchanged, the command timeout applies, and the command's reply is returned instead of the `ASKING` reply (an error reply to `ASKING` is raised).
* Fixed command metrics classification: every `ReThisException` subclass (cluster redirects, parse and protocol errors) is now recorded as `ERROR_REDIS` instead of `ERROR_OTHER`, and a truncated RESP frame (`RespUnexpectedEOF`) is recorded as `ERROR_IO`; timeouts remain `ERROR_TIMEOUT`.
* Fixed retried pipelines (and the cluster `ASK` replay) resending empty requests: `doBatchRequest` drained the payload buffers on the first write, so a retry sent 0 bytes and waited for replies that never came. It now writes a copy of each payload, like `doRequest`, at the cost of one buffer copy per pipelined command.
* Fixed cluster pipelines returning responses grouped by slot instead of in request order when requests with different slots are interleaved; responses are now placed back at the position of their request.
* Fixed RESP3 doubles `inf`, `-inf` and `nan` (and the RESP2 string `inf`/`-inf` forms) failing to parse in the double decoder, the generic response reader, `ZPopResultDecoder` and `GeoPosDecoder`; they now map to the corresponding `Double` values and malformed doubles raise `ResponseParsingException`.
* Fixed RESP3 verbatim strings: `VerbatimStringDecoder` now strips the 3-byte format and `:` prefix (`txt:Some string` decodes to `Some string`, affecting replies such as `INFO`, `CLIENT LIST` and `LATENCY DOCTOR`), and the generic response reader reads the content by its declared length so multi-line verbatim strings no longer truncate at the first newline and desynchronise following array elements; frames shorter than the 4-byte prefix raise `ResponseParsingException`.
* Fixed the RESP stream framer accepting malformed numeric headers: a bare `-`, repeated `-` or an empty length (`*-\r\n`, `*--5\r\n`, `$\r\n`) now raise `RespProtocolException` instead of being framed as an empty aggregate; also fixed single-line replies ending in `CR CR LF` running on into the next frame.
* Fixed generated command codecs (`ZSCORE`, `ZINCRBY`, `ZADD` with `INCR`, `HINCRBYFLOAT`, `INCRBYFLOAT`, `GEODIST`) throwing `NumberFormatException` on a RESP2 `inf`/`-inf` bulk reply; `String.toRespDouble()` is now public in `shared` for generated code.
* Fixed the Sentinel topology refresh aborting when a replica latency probe failed under `ReadFrom.LowestLatency`: the failure is logged, the replica is recorded as the slowest and never picked, and the topology is still published.
* Fixed Sentinel failover never being detected reactively: the `+switch-master` subscription now runs independently of the periodic refresh loop (previously it was unreachable when `periodicRefresh` was enabled), is opened on the configured sentinels instead of the data nodes, uses the real `+switch-master` channel name instead of the enum constant name, and refreshes the topology only for messages about the monitored master.
* Fixed `SENTINEL REPLICAS` parsing in the Sentinel topology manager: replicas are now read by field name (`ip`, `port`, `flags`) from the reply maps instead of by position as if it were a `CLUSTER NODES` line, which failed the whole refresh, and replicas flagged `s_down`, `o_down` or `disconnected` are no longer kept in the read pool; malformed entries are logged and skipped.
* Fixed cluster slot routing for slots no node covers: the slot-owner table now marks them as uncovered instead of silently routing to the first node, and master and replica reads fail immediately with `ClusterException("No master for slot N")` instead of reaching a wrong node or indexing past the provider array.
* Fixed `ReadFrom.Replica` leaking `NoSuchElementException` when no replica exists (Sentinel with only the master, or a cluster slot whose master has no replicas): it now throws a `ReThisException` naming the missing replicas and stays strict, with no fallback to the master.
* Fixed the cluster client waiting `movedBackoffPeriod` (5 s by default) before surfacing every non-redirect error: permanent errors such as `WRONGTYPE` or decode failures are now rethrown immediately, and the back-off applies only to I/O errors, command timeouts, unexpected end of stream, `MOVED` and the unstable/down cluster-state errors.
* Fixed `unwrapRESPAgnosticMap` producing a bogus `k -> k` entry for an odd-length RESP2 array (it now throws `ResponseParsingException`), and `unwrapList` silently dropping elements of the wrong type (it now throws `ResponseParsingException`; `RType.Null` elements are still skipped).
* Fixed the RESP stream framer miscounting a RESP3 attribute nested inside an aggregate: the attribute is now treated as metadata preceding the real element instead of consuming one of the parent's elements, which left bytes in the stream and desynchronised the connection (latent: Redis servers do not currently emit nested attributes).
* Nullable string and byte-array replies inside a pipeline or transaction now decode the queued placeholder as `null` instead of an empty string / empty array.
* Behaviour change: command retries now apply only to transient failures. Connection/IO errors, truncated frames and cluster redirects or unstable/down cluster states are retried; `CommandTimeoutException` is retried only for read commands (a timed-out write may already have been applied and is no longer re-sent); Redis error replies (`WRONGTYPE`, `NOAUTH`, ...), parse errors and other permanent `ReThisException`s are surfaced immediately instead of after `retry.times` attempts; coroutine cancellation is rethrown at once and no longer counted as a failed attempt.
* Fixed topology refresh requests ignoring `commandTimeout`: the cluster `CLUSTER SLOTS` fetch and the Sentinel master/replica/latency probes now honour it, so a seed or sentinel that accepts the connection but never replies no longer stalls the refresh (the cluster fetch moves on to the next seed, the Sentinel fetch to the next sentinel). `commandTimeout` still defaults to `null` (unbounded).
* Fixed an enclosing timeout (for example `withTimeoutOrNull`) around a command being reported as `CommandTimeoutException` when `commandTimeout` is set: the caller's cancellation is now preserved and only the command's own deadline raises `CommandTimeoutException`.
* Fixed `ArrayLongDecoder.decodeNullable` reading only the first element of a nullable integer array and leaving a null element's CRLF unread (RESP3 `_` and RESP2 `$-1` nulls are now consumed per element), and `ArrayByteArrayDecoder.decodeNullable` skipping the `$` byte of each element when the array header was already consumed.
* Retry scope note: server error replies such as `LOADING` and `MASTERDOWN`, a connection-pool acquire timeout, and `Cluster not ready` are no longer retried by `withRetry`; only connection/IO failures, cluster redirects and cluster-state errors are (and command timeouts only for read commands).
* Fixed the connection pool idle counter drifting upward on every borrow: the counter is now decremented when a connection is taken from the idle channel, so pool expansion under load, shrinking and the `onPoolSample` idle metric work from the real idle size, and the counter is raised before a connection is offered to the idle channel so a concurrent borrow can no longer drive it negative.
* Fixed the pool burst-rate calculation truncating `pool.checkInterval` to whole seconds: a sub-second interval (for example `500.milliseconds`) produced an infinite rate and expanded the pool to the connection cap, and `1500.milliseconds` overstated the rate by 50%. The rate now uses fractional seconds, one adjustment adds at most `maxIdleConnections - idle` connections, and `checkInterval` must be positive: a non-positive value is rejected with an `IllegalArgumentException` when the pool is created.
* Fixed the inverted `ConnectionPool.haveIdleConnections()` check: it reported spare connections for an empty pool and none for a full one. It now reports whether at least one idle connection is held, which changes `ReadFrom.MasterPreferred` routing: reads go to the master only when its pool has an idle connection, otherwise to a replica.
* Fixed `pool { setClientName = true }` being ignored on RESP2 connections without auth or a non-zero db: the connection setup (`HELLO 2 SETNAME`) is no longer skipped when a client name is requested; on RESP2 this requires Redis 6 or newer, as `HELLO` does. A negative `db` is now rejected with an `IllegalArgumentException` instead of being silently treated as no db.
* Fixed Kotlin/Native creating a new `IO` thread-pool dispatcher on every read of the default dispatcher (per client configuration and per pool fill), which accumulated threads and memory and left threads running; the dispatcher is now created once and shared process-wide (its size is unchanged: the larger of the core count and 64 threads).
* Behaviour change: `StreamFlow` now acknowledges a message only after `emit` returns normally, giving at-least-once delivery: a message whose collector throws or is cancelled, or whose emit is aborted (for example the last element of `take(n)`, or a rejected item of `takeWhile`), stays pending in the group instead of being lost, and a handler may see a message again. Pending entries are not redelivered automatically (the flow reads only new messages), so they must be claimed separately.
* Fixed `StreamFlow` sleeping 100 ms after every non-empty batch when `block` is zero: the poll delay now applies only after an empty read, so a backlog is drained at full speed.
* Fixed `PubSubEventParser.parse` throwing `IndexOutOfBoundsException` or `NullPointerException` in the pub/sub reader loop on a truncated push frame (for example `["message", "ch"]`) or a frame with a channel or count of the wrong type: such frames are now ignored (`null`), like unknown markers.
* **Behaviour change:** `PEXPIRE` previously sent the `Duration` in whole seconds, so TTLs were 1000x shorter than requested (`pExpire("k", 10.seconds)` set a 10 ms TTL and sub-second durations deleted the key at once). It now sends milliseconds. Callers who compensated by passing a `Duration` 1000x larger will now get TTLs 1000x longer and must remove the workaround.
* Fixed `sentinelCkQuorum`, `sentinelReset` and `clusterBumpEpoch` (all still return `Boolean`) never reporting success: `sentinelCkQuorum` is now `true` when the reply starts with `OK` (previously the full `OK <n> usable Sentinels...` message never equalled `OK`, so it was always `false`); `sentinelReset` now decodes the integer reply and is `true` when at least one master was reset (previously it threw `UnexpectedResponseType` even though the reset happened); `clusterBumpEpoch` is `true` for `BUMPED <epoch>` and `false` for `STILL <epoch>` (previously always `false`).
* Build tooling: the code generator now downloads the Redis command spec from a pinned `vendelieu/redis-spec` commit (the `redis-spec` version in `libs.versions.toml`) instead of the upstream `master` branch, with connect and request timeouts, and fails with the URL and status on a non-200 response.
* Fixed contended `reDistributedLock` / `reHierarchicalDistributedLock` acquisition hot-looping against Redis: a waiter now subscribes once per `tryLock` call to the lock's release channel and retries when a release is published or after the backoff delay (whichever comes first), and releases only its own subscription when `tryLock` returns, is cancelled or times out.
* Fixed the distributed lock watchdog: a failed lease refresh no longer cancels the caller (`reHierarchicalDistributedLock` cancelled its whole reference job) or silently stops renewing; transient failures are logged and retried on the next tick, and leases shorter than 3 ms no longer make the watchdog refresh in a tight loop.
* The code generator now fails the build when a command spec's declared `operation` or `isBlocking` disagrees with the upstream command flags; each mismatch is reported as a KSP error naming the command (previously mismatches were collected and silently dropped).
* The code generator now reports a build error for a `Duration`/`Instant` command parameter without an explicit `@OutgoingTimeUnit` (instead of silently assuming milliseconds), accepts the unit as `SECONDS` or `TimeUnit.SECONDS`, and reports spec-matched parameters it cannot write instead of dropping them silently. `MIGRATE` now declares its timeout unit explicitly (milliseconds, unchanged).
* Fixed `reHierarchicalDistributedLock` losing reentrancy depth updates when sibling coroutines locked or unlocked concurrently, which freed the key too early and stopped the watchdog while the lock was still held.
* `withLock` now preserves the block's exception when unlock also fails: the `LockLostException` is attached as a suppressed exception instead of replacing it.
* Fixed nested `unlock()` of `reDistributedLock` / `reHierarchicalDistributedLock` resetting the key TTL to the default lease instead of the lease the lock was acquired with.
* Fixed the lock watchdog outliving a lock whose `unlock()` failed with `LockLostException`: the local state is now cleared and the watchdog stopped.
* Fixed `unlock()` of `reDistributedLock` / `reHierarchicalDistributedLock` clearing the state or cancelling the watchdog of a coroutine that acquired the lock right after the release, which made that coroutine's own `unlock()` fail or left its lease unrefreshed.
* Fixed the lock watchdog wiping the state of a newer owner when its last refresh reported the lock lost after another coroutine had already re-acquired it, and a cancelled `lock()` leaking a hold counted in Redis on `reHierarchicalDistributedLock`.
* Added `cfLoadChunkBA`, a `ByteArray` payload variant of `CF.LOADCHUNK`: the chunks returned by `CF.SCANDUMP` are binary and a `String` payload corrupts bytes that are not valid UTF-8 (the server rejected the restored chunk).
* Added `bfLoadChunkBA`, a `ByteArray` payload variant of `BF.LOADCHUNK`: the chunks returned by `BF.SCANDUMP` are binary and a `String` payload corrupts them (the server rejected every restored chunk).
* **Behaviour change: server error replies decoded by typed codecs and decoders now throw the server's own `ReThisException` instead of a `UnexpectedResponseType` / `ResponseParsingException` wrapper.** `WAITAOF` without appendonly, `SCRIPT KILL` / `FUNCTION KILL` (`NOTBUSY`), `PING` on a script-busy server (`BUSY`) and every other command with a fixed response set now surface the server text as the exception message (and the typed cluster exceptions such as `RedirectMovedException` are no longer hidden under a wrapper); previously the text was only available on `.cause`. Callers that caught `UnexpectedResponseType` or `ResponseParsingException` for server errors must catch `ReThisException`. A reply that is not an error but has the wrong shape still throws `ResponseParsingException` / `UnexpectedResponseType`.
* Fixed RESP3 verbatim string replies failing to decode for `LATENCY GRAPH`, `CLUSTER INFO` and `CLUSTER NODES`: their specs only declared a bulk string, so the generated codecs threw `UnexpectedResponseType: Expected [BULK] but got VERBATIM_STRING` on RESP3 connections (verified against Redis 8.4).
* **Behaviour change: commands whose spec declares a null reply next to an array now return `null` for a RESP2 null array (`*-1`), like RESP3 `_`, instead of an empty list.** Affected: `lPopCount`, `rPopCount`, `exec` (aborted by `WATCH`), `zRankWithScores` / `zRevRankWithScore` (missing member), `bitfield`, `zMScore`, `xRead` (blocking timeout), `vRange` and `aclGetUser`. A real empty array (`*0`) still decodes to an empty list, and commands whose spec does not declare a null reply keep decoding `*-1` as an empty list. Callers that used `.orEmpty()` are unaffected; callers that compared a RESP2 result with an empty list must also handle `null`.
* Fixed error inference treating any error text that merely starts with `ASK`, `MOVED`, `CROSSSLOT`, `TRYAGAIN` or `CLUSTERDOWN` as that cluster error: the code must now be the whole first token (`ASKED ...` stays a plain `ReThisException`).
* Fixed `CROSSSLOT` errors dropping the server text: `CrossSlotOperationException.message` is now the full server message (`CROSSSLOT Keys in request don't hash to the same slot`) instead of `CROSSSLOT operation`.
* Fixed the RESP3 bulk-error cause inference always decoding as UTF-8: `tryInferCause` takes an optional `charset` (default UTF-8) and the shared decoders pass the configured one.
* Fixed `XReadGroupDecoder.decodeNullable` returning an empty list for a RESP2 null array (`*-1`, a timed out `XREADGROUP ... BLOCK`) while RESP3 returned null; it now returns null for both.
* Fixed a malformed bulk string, bulk error or verbatim string frame (negative or oversized declared length, missing trailing CRLF) surfacing as an `IllegalArgumentException` or `EOFException`, which the retry logic classifies as a transient transport failure; it now throws `ResponseParsingException`.
* Fixed the RESP3 response framer desynchronising the connection when several attribute frames (`|...`) precede a reply: all leading attributes are now routed to the attributes buffer and the real reply is framed.
* Documented that `WATCH` / `UNWATCH` are connection-scoped (KDoc on `ReThis.transaction`): through the pooled client `watch` marks one connection while the next call or `transaction` may run on another, so optimistic locking needs the keys watched on the connection the transaction uses. `WATCH`, `UNWATCH`, `MULTI`, `EXEC` and `DISCARD` sent through the pool outside a transaction now log a warning; behaviour is unchanged and no pinned-connection API was added (left as an owner decision).
* Documented the RESP3 verbatim string split: `VerbatimString.data` is the bare content (what the typed decoders return) and `VerbatimString.value`, which `unwrap<String>()` returns, is `format:data`; `unwrap<String>()` is deliberately unchanged.
* A RESP2 connection that sets the client name or authenticates now falls back to `AUTH` / `CLIENT SETNAME` when the server does not know `HELLO` (Redis older than 6), remembering that for later connections; RESP3 against such a server fails with a message saying it requires Redis 6 or newer.
* `ReThisSet.clear()` now deletes the set key with a single `DEL` (like `ReThisMap.clear()`) instead of `SCARD` followed by `SPOP`, which was not atomic.
* Fixed cluster routing of shard pub/sub: `sPublish` and `sSubscribe` hashed no slot and always went to the first node (answered with `MOVED` unless it owned the channel); the shard channel is now hashed like a key so the request reaches the owning node.
* Fixed cluster `pipeline { }` splitting a `MULTI` .. `EXEC` block across nodes (the keyless `MULTI` and `EXEC` were sent apart from the keyed commands): the block is now kept together and in order on the node owning its slot, a block whose keys span several slots throws `CrossSlotOperationException` before anything is sent, and commands of a block are never replayed individually after a `MOVED` reply.
* Fixed `ReadFrom.AnyReplica` and `ReadFrom.LowestLatency` ignoring the key's slot on a cluster: a keyed read now goes to a replica of the shard owning the slot (the lowest measured latency for `LowestLatency`, unmeasured nodes ranking last), or to the shard's master when it has no replicas, instead of any replica of the cluster or the first node. Keyless commands keep the previous behaviour, and `AnyReplica` now fails fast with `ClusterException` for a slot no node covers like the other strategies.

### Breaking changes
* `zRevRankWithScore` now returns `List<RType>?` (rank and score) instead of `List<Long>?`, which failed to decode the double score, and `withScore` is now an optional `Boolean?` like `zRankWithScore`; `withScore = false` previously sent a malformed request.
* `zPopMax` now returns the raw `List<RType>` reply like `zPopMin` (flat member and score elements on RESP2 and with no count, one `[member, score]` array per entry on RESP3 with a count) instead of `List<MPopResult>`, which put the member in the key-name field on RESP2 and threw on RESP3 with a count.
* `zIncrBy` now takes a `Double` increment instead of a `Long` (source-breaking), so fractional increments such as `0.5` can be sent.
* `zInter`, `zUnion`, `zInterStore` and `zUnionStore` now take `weight: List<Double>?` instead of `List<Long>?` (source-breaking), so fractional `WEIGHTS` can be sent.
* `zCount` and `zRemRangeByScore` now take `String` bounds instead of `Double`, so exclusive (`(5`) and infinite (`-inf`, `+inf`) score bounds can be expressed; callers passing doubles must pass their string form (for example `"0"`, `"2.5"`).
* `lMove` and `lMoveBA` now return `String?` / `ByteArray?` (null when the source list does not exist), and `lPopCount` and `rPopCount` now return `List<String>?` (null for a missing key on both RESP2 and RESP3) and require `count: Long` (source-breaking); the no-count form is `lPop` / `rPop`, which the old nullable `count` default sent as a bare `LPOP key` and failed to decode.
* `sRandMember` and `sRandMemberBA` now return `String?` / `ByteArray?` (null for a missing key), and `sRandMemberCount` and `sPopCount` now require `count: Long` (source-breaking); the nullable `count` default omitted `COUNT`, which makes Redis reply with a single bulk string that the array decoder rejected. Use `sRandMember` / `sPop` for the no-count form.
* `zRandMember` and `zRandMemberBA` now return `String?` / `ByteArray?` (null for a missing key) instead of throwing on the nil reply (source-breaking for callers relying on a non-null result).
* `geoHash`, `hGetDel` and `hGetEx` now return `List<String?>` instead of `List<String>` (source-breaking): a member or field that does not exist is reported as a `null` element instead of failing the whole call with a parsing exception, like `hMGet`.
* `bzPopMin` and `bzPopMax` now return `null` on a blocking timeout instead of failing (`IndexOutOfBoundsException` on RESP2, a parsing exception on RESP3), and `ZPopResultDecoder` is now a `ResponseDecoder<ZPopResult?>` (source-breaking for direct callers of the decoder); a malformed non-empty reply throws `ResponseParsingException`.
* `jsonSet` (both the plain and the serde overloads) now returns `String?` and `sentinelGetMasterAddr` now returns `List<String>?` (source-breaking): `JSON.SET` with `NX`/`XX` whose condition fails returns `null` instead of throwing `UnexpectedResponseType` (on RESP2 and RESP3), and `SENTINEL GET-MASTER-ADDR-BY-NAME` for an unknown master returns `null` instead of an empty list (RESP2 `*-1`) or a parsing failure (RESP3 `_`); the Sentinel topology refresh now reports an unknown master by name.
* `ftTagVals` and `ftDictDump` now return `Set<String>` instead of `List<String>` (source-breaking): on RESP3 both commands reply with a set (`~`), which the previous list-only decoding rejected with `UnexpectedResponseType`; they now decode both the RESP2 array and the RESP3 set.
* `tDigestByRank` and `tDigestByRevRank` now take `Long` ranks instead of `Double` (source-breaking): `TDIGEST.BYRANK`/`BYREVRANK` take integer ranks and the server rejected the previous `0.0` form with `T-Digest: error parsing rank`.
* `DelExOption.IfDigestEq` and `DelExOption.IfDigestNe` now take the digest as a hex `String` instead of a `Long` (source-breaking): the 16-character hex value returned by `digest` can now be passed back to `delEx` unchanged; the previous decimal form was rejected by the server.
* `tsRange`, `tsRevRange`, `tsMRange` and `tsMRevRange` now take `bucketTimestamp: TsBucketTimestamp?` (`Low`, `High`, `Mid`) instead of a `Boolean?` (source-breaking): `BUCKETTIMESTAMP` requires a `-`/`+`/`~` argument, so the bare flag was rejected by the server with `wrong number of arguments`.
* `lcsDetailed` now returns `LcsResult.matches` as `List<LcsMatch>` with one entry per matching substring, and `LcsMatch` carries both ranges (`first: LongRange`, `second: LongRange`) plus the optional `length` instead of `start`/`end` (source-breaking): the previous decoder dropped the second key's range without `WITHMATCHLEN` and returned the same match as two entries with `WITHMATCHLEN`; the shape is now the same in both modes and on RESP2 and RESP3.
* `ReThisMap.put`, `remove` and `setValue` now return the previous value (`null` when the field did not exist) instead of the new value or always `null`; `put` now returns `String?` (source-compatible for most callers, binary-incompatible). `put` and `setValue` cost one extra `HGET` round trip and are not atomic; `remove` runs an `EVAL` script (`HGET` + `HDEL`, atomic, works on any Redis version).
* The blocking `ReThisMap` / `ReThisSet` wrappers (`ReThis.Hash`, `ReThis.Set`) now throw `UnsupportedOperationException` on JS and wasmJs instead of returning a `Deferred` masquerading as the result (JS) or throwing `IllegalStateException` (wasmJs); use the suspend API on those targets. Graceful pool close (`closeGracefully`) falls back to an immediate close there.
* **Behaviour change: cluster clients now route every keyed command to the node owning the key's hash slot** (to a replica of that slot's master for reads under `ReadFrom.Replica`, `ReplicaPreferred`, `MasterPreferred`), instead of sending everything to the first node and relying on `MOVED`. Keyless commands still go to the first node. Hash tags are honoured when computing the slot (`MGET {user1}a {user1}b` is one slot; new public `CRC16.slot`), keys in different slots raise `CrossSlotOperationException` naming both slots, `MOVED`/`ASK`/`TRYAGAIN`/`CLUSTERDOWN` error replies are now raised inside the retried call so they are actually followed (previously they surfaced from the codec without a retry), a pipeline slot group that contains a write is sent to the master, and a pipeline retries commands answered with `MOVED` once on the new owner (other redirects, `ASK` and retries are not followed for pipelined commands). Script and function calls with no keys (`EVAL ... 0`, `FCALL fn 0`) are sent without a slot and run on any node. Cluster connections now send `READONLY` when a replica read strategy is configured, so replicas accept the routed reads; an `ASK` target that answers `MOVED` is followed. This has only been verified against fake nodes: a manual smoke test against a live multi-master cluster is still owed.
* `blPop` and `brPop` now return `null` on a blocking timeout instead of a dummy `PopResult("", "")` (the declared return type was already `PopResult?`), and `LPopDecoder` is now a `ResponseDecoder<PopResult?>` (source-breaking for direct callers of the decoder); a malformed non-empty reply still throws `ResponseParsingException`.
* Fixed a rejected cluster `ASKING` disposing the ask target's connection even though it was still protocol-clean: the connection is now released to the pool and the `ClusterException` raised afterwards. A server error reply never disposed a pooled connection (it is returned as data and raised by the codec after the connection is released); this is now covered by tests and documented on `withConnection`.
* Hardened Sentinel topology handling: the `+switch-master` refresh is skipped when the snapshot already points at the announced master (so one failover announced by N sentinels causes one refresh instead of up to N, and a message arriving during a refresh is no longer dropped); sentinel subscriptions are retried, and re-opened after the connection drops, with the `retry` backoff (`initialDelay` growing by `factor` up to `maxDelay`, no attempt limit) until the client is closed; sentinel providers are stored atomically and a provider created while the client is closing is closed instead of leaked; a replica latency probe is bounded to 2 seconds (`LATENCY_PROBE_TIMEOUT`) and recorded as the slowest when it does not answer; an integer-typed `port` in a `SENTINEL REPLICAS` entry is accepted; and the `Unable to fetch Sentinel topology` error now carries the last failure as its cause.
* Hardened cluster topology handling: a `MOVED` redirect now publishes a new topology snapshot (copy-on-write) instead of rewriting the slot table that concurrent requests are reading, and a redirect naming a slot outside `0..16383` is ignored instead of failing with `ArrayIndexOutOfBoundsException`; a timeout inside an on-demand topology fetch is treated as a failed fetch (with the usual one second cool-down) instead of escaping as a cancellation.
* **Behaviour change: retry classification.** The cluster error raised while no topology is known yet (`Cluster not ready`, for example during a failover) is now retried like other transient failures; `No master for slot`, `CROSSSLOT` and absent-key errors stay permanent. `SingleConnectionProvider` now fails with an `IOException` (previously a plain `ReThisException`) when it cannot create a connection, so that failure is retried as before.
* **Behaviour change: connection acquire timeout.** When no pooled connection frees up within `connectionAcquireTimeout`, the call now fails with `CommandTimeoutException` (cause: the `TimeoutCancellationException`) instead of a raw `TimeoutCancellationException`. It follows the command-timeout retry rule: reads are retried, writes are not (nothing was sent, but retrying would multiply the wait on an exhausted pool). A timeout or cancellation imposed by the caller is still a cancellation.
* Fixed the active-subscription metric drifting: `onSubscriptionChange` now emits `+1` only when a target becomes active (previously once per worker) and `-1` once when it is removed, including when it is removed by `unsubscribe`.
* Fixed connection pool prepopulation leaking connect failures: a refused connection or failed handshake while filling `minIdleConnections` is now logged, recorded as a failed connection creation and absorbed, instead of reaching the uncaught-exception handler (which could crash the process on Android).
* A command issued during the on-demand topology fetch cool-down (`Cluster not ready`) now waits out the remainder of the cool-down once and takes part in the next fetch instead of failing at once with the cached cause, so retries can outlast a short failover; a full topology refresh no longer discards a concurrent `MOVED` update or leaks its provider.
* `ClientKillOptions.MaxAge` now takes a `Duration` (the maximum client age) instead of an `Instant`: `CLIENT KILL MAXAGE` is an age in seconds, so the epoch seconds that were sent closed nothing. The property is renamed from `instant` to `maxAge` (source and binary breaking).
* `StreamFlow` now re-reads this consumer's own pending entries (delivered earlier under the same consumer name but never acknowledged, e.g. after a failed or cancelled emit or a restart) on every start before switching to new messages. They are emitted and then acknowledged, so delivery is at-least-once and an entry processed just before a crash can be seen twice. The signature and defaults are unchanged; with `acknowledge = false` pending entries are delivered once per start and stay pending.
* `StreamFlow` has a new opt-in trailing parameter `claimIdleAfter: Duration? = null` (the default keeps the previous behaviour, existing calls stay source compatible, the Kotlin signature gains a defaulted parameter so the JVM method signature changes: binary-incompatible on the JVM, callers must be recompiled; a negative `claimIdleAfter` makes the collection fail with `IllegalArgumentException`). When set, the flow also takes over, with `XAUTOCLAIM` (Redis 6.2+), entries that have been pending for other consumers longer than the given idle time: once at start after the own-pending drain, then about every 5 seconds between reads, paging until the cursor is `0-0`. Claimed entries are emitted and then acknowledged. Delivery stays at-least-once: the original consumer may still process an entry that was taken over.
* Fixed `StreamFlow` emitting an empty message for a pending entry that was deleted from the stream (`XDEL`, trimming) before it was re-read: such an entry is now skipped and acknowledged.
* `lmPop`, `blmPop`, `zMPop` and `bzMPop` now return `null` when no element was popped (a `BLMPOP`/`BZMPOP` timeout, or an empty or missing key) on both RESP2 and RESP3, instead of an empty list, consistent with `blPop`/`brPop`; the declared return type was already `List<MPopResult>?`. `LMPopDecoder` is now a `ResponseDecoder<List<MPopResult>?>` (source-breaking for direct callers of the decoder). Callers that tested the result with `isEmpty()` must check for `null`.

## 0.4.5

* Fixed retries resending an empty request: the first attempt's socket write drained the request buffer, so every retry sent 0 bytes and waited for a reply until the connection dropped or the command timed out. `doRequest` now writes a copy of the payload, so each attempt sends the full command ([#168](https://github.com/vendelieu/re.this/issues/168)).
* Fixed `retry { times = N }` making the wrong number of attempts (1 for `N = 1` or `2`, `N - 1` for `N >= 3`) and sleeping once more after the last failed attempt before throwing. It now makes exactly `N` attempts (at least one) with no backoff after the final one; failures of earlier attempts are attached to the thrown exception as suppressed. This also applies to the cluster topology fetch.
* Fixed ordering of platform KSP tasks relative to common metadata generation.
* Updated dependencies: Kotlin 2.4.21, Ktor 3.6.1, kotlinx-io 0.9.2, kotlinx-serialization 1.12.0, KotlinPoet 2.4.0, KSP 2.3.13. Build and test tooling: Gradle 9.9.0, Dokka 2.3.0, kotlinter 5.7.0, Kover 0.9.11, binary-compatibility-validator 0.18.2, Kotest 6.2.6, Logback 1.6.6, testcontainers-redis 3.0.1.

## 0.4.4

* Fixed multi-element reply decoding (`HGETALL` and other map/array string replies) misplacing the read cursor after large, multi-line, or null bulk-string values: bulk and verbatim string payloads are now consumed by their declared RESP byte size instead of line-based reads or charset-dependent `readText` limits, which caused `IllegalArgumentException: No suitable message type found` mid-payload (notably on Kotlin/Native with values ~30 KB+).
* Fixed RESP3 null elements (`_\r\n`) in nullable array/map decoding leaving their trailing CRLF unconsumed, corrupting the position of subsequent elements.
* Changed `unwrap()` to throw `ResponseParsingException` when called on an aggregate reply (`RArray`, `RSet`, `Push`, `RMap`) instead of silently returning `null`; the message points to `unwrapList()`/`unwrapSet()`/`unwrapMap()`/`unwrapRESPAgnosticMap()`. `RType.Null` now unwraps to `null` without a misleading warning log.
* Fixed `zMPop`/`bzMPop`/`zPopMax` silently returning empty `poppedElements`: popped members and their scores are now included (scores rendered as strings).
* Fixed commands issued inside pub/sub handlers (localized-connection scope) corrupting the reply frame: the internal error-peek consumed the reply's type byte and re-appended it to the buffer tail.
* Fixed `ClusterSlotsDecoder` being unable to parse a real `CLUSTER SLOTS` reply (it expected lines without RESP type prefixes and flat node fields instead of nested node arrays), which broke cluster topology discovery; it now decodes the actual wire format for both RESP2 (metadata array) and RESP3 (metadata map) and merges slot ranges per master.
* Fixed `ClusterShardsDecoder` (`clusterShards()`) with the same defect: it expected prefix-less lines and an uppercase `health` value; it now decodes real RESP2 key-value-array and RESP3 map shard replies, including null/empty `endpoint`/`hostname` and lowercase `health`.
* Performance: the response framing reader now scans buffered spans instead of issuing per-byte channel reads; RESP size lines are parsed directly from bytes without an intermediate `String`; bulk-string payload decoding takes a zero-copy UTF-8 fast path; `BulkString.equals`/`hashCode` memoize the payload snapshot (also making them stable after the buffer is consumed).
* Added in-memory RESP framing/decode benchmarks (`benchmarks:mainDecodeBenchmark`).

## 0.4.3

* Added Redis Bloom commands: `BF.ADD`, `BF.CARD`, `BF.EXISTS`, `BF.INFO`, `BF.INSERT`, `BF.LOADCHUNK`, `BF.MADD`, `BF.MEXISTS`, `BF.RESERVE`, `BF.SCANDUMP`.
* Added Redis Cuckoo Filter commands: `CF.ADD`, `CF.ADDNX`, `CF.COUNT`, `CF.DEL`, `CF.EXISTS`, `CF.INFO`, `CF.INSERT`, `CF.INSERTNX`, `CF.LOADCHUNK`, `CF.MEXISTS`, `CF.RESERVE`, `CF.SCANDUMP`.
* Added Redis Count-Min Sketch commands: `CMS.INCRBY`, `CMS.INFO`, `CMS.INITBYDIM`, `CMS.INITBYPROB`, `CMS.MERGE`, `CMS.QUERY`.
* Added Redis t-digest commands: `TDIGEST.ADD`, `TDIGEST.BYRANK`, `TDIGEST.BYREVRANK`, `TDIGEST.CDF`, `TDIGEST.CREATE`, `TDIGEST.INFO`, `TDIGEST.MAX`, `TDIGEST.MERGE`, `TDIGEST.MIN`, `TDIGEST.QUANTILE`, `TDIGEST.RANK`, `TDIGEST.RESET`, `TDIGEST.REVRANK`, `TDIGEST.TRIMMED_MEAN`.
* Added Redis TopK commands: `TOPK.ADD`, `TOPK.INCRBY`, `TOPK.INFO`, `TOPK.LIST`, `TOPK.QUERY`, `TOPK.RESERVE`.
* Added Redis TimeSeries commands: `TS.ADD`, `TS.ALTER`, `TS.CREATE`, `TS.CREATERULE`, `TS.DECRBY`, `TS.DEL`, `TS.DELETERULE`, `TS.GET`, `TS.INCRBY`, `TS.INFO`, `TS.MADD`, `TS.MGET`, `TS.MRANGE`, `TS.MREVRANGE`, `TS.QUERYINDEX`, `TS.RANGE`, `TS.REVRANGE`.
* Added RediSearch commands: `FT.AGGREGATE`, `FT.ALIASADD`, `FT.ALIASDEL`, `FT.ALIASUPDATE`, `FT.ALTER`, `FT.CREATE`, `FT.CURSOR DEL`, `FT.CURSOR READ`, `FT.DICTADD`, `FT.DICTDEL`, `FT.DICTDUMP`, `FT.DROPINDEX`, `FT.EXPLAIN`, `FT.EXPLAINCLI`, `FT.HYBRID`, `FT.INFO`, `FT.PROFILE`, `FT.SEARCH`, `FT.SPELLCHECK`, `FT.SUGADD`, `FT.SUGDEL`, `FT.SUGGET`, `FT.SUGLEN`, `FT.SYNDUMP`, `FT.SYNUPDATE`, `FT.TAGVALS`, `FT._LIST`.
* Added JSON command: `JSON.DEBUG MEMORY`.
* Removed deprecated `ClientType.Slave` alias; use `ClientType.Replica`.
* Fixed KSP processor `isWithinBounds` IndexOutOfBoundsException when matching nested option params with empty parent bounds.
* Improved KSP token-to-identifier translation to safely handle tokens containing `.`, `+`, `-`, and quote characters (renamed the generated empty-token constant from `EMPTY` to `EMPTY_STRING`).

## 0.4.2

* Added metrics reporting through `metricsRecorder` configuration option.
* Added cluster commands: `CLUSTER MIGRATION`, `CLUSTER SLAVES`, `CLUSTER SLOT-STATS`, `CLUSTER SLOTS`, `CLUSTER SYNC-SLOTS`.
* Added connection commands: `CLIENT CACHING`, `CLIENT GETREDIR`, `CLIENT NO-EVICT`, `CLIENT NO-TOUCH`, `CLIENT SETINFO`, `CLIENT TRACKINGINFO`, `CLIENT UNPAUSE`, `ECHO`, `QUIT`, `RESET`.
* Added hash commands: `HGETDEL`, `HGETEX`, `HMSET`, `HPEXPIRE`, `HPEXPIREAT`, `HPEXPIRETIME`, `HSETEX`.
* Added JSON commands: `JSON.NUMMULTBY`, `JSON.RESP`.
* Added sentinel commands: `SENTINEL DEBUG`, `SENTINEL IS-MASTER-DOWN-BY-ADDR`.
* Added server commands: `COMMAND DOCS`, `COMMAND INFO`, `COMMAND LIST`, `CONFIG GET`, `CONFIG RESETSTAT`, `CONFIG SET`, `DBSIZE`, `DEBUG`, `HOTKEYS GET/RESET/START/STOP`, `INFO`, `LOLWUT`, `SFLUSH`, `TRIM-SLOTS`.
* Added stream commands: `XACKDEL`, `XCFGSET`, `XDELEX`, `XNACK`.
* Added string commands: `DELEX`, `DIGEST`, `MSETEX`.
* Added vector commands: `VADD`, `VCARD`, `VDIM`, `VEMB`, `VGETATTR`, `VINFO`, `VISMEMBER`, `VLINKS`, `VRANDMEMBER`, `VRANGE`, `VREM`, `VSETATTR`, `VSIM`.
* Improved int/long/duration/instant encoding in codecs.
* Improved internal codecs building logic.

## 0.4.1

* Added per-command timeout via `commandTimeout` configuration option, throwing `CommandTimeoutException` when a Redis reply exceeds the deadline.
* Added `connectionHealthCheckInterval` configuration option to skip PING on recently-used connections.
* Added cluster and sentinel integration tests.
* Fixed acquire/release cancellation race in `ConnectionPool` that could leak connections delivered to cancelled awaiters.
* Fixed `ConnectionPool` health-check to use the configured charset instead of hardcoded UTF-8.
* Fixed pubsub handler exceptions in `onMessage`/`onSubscribe`/`onUnsubscribe` crashing the subscription loop; now centrally routed to `onException` on both the handler and global handlers.

## 0.4.0

* Fixed `XREADGROUP` decoding.
* Improved token handling in codecs.
* Improved varying argument handling in codecs.
* Improved subscriptions user interface.
* Returned default path (`.`) for json commands.
* Fixed `Null` parsing in wrapped (`RType`) parser.

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