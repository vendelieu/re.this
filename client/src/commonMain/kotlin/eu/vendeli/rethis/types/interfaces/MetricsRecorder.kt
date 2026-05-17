package eu.vendeli.rethis.types.interfaces

@RequiresOptIn(message = "Metrics API is experimental and may change.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalReThisMetricsApi

/**
 * Pluggable sink for runtime telemetry emitted by a [eu.vendeli.rethis.ReThis] client.
 *
 * The recorder is opt-in: leave `cfg.metricsRecorder = null` (the default) and every
 * instrumentation site in the library short-circuits via `?.`, paying nothing —
 * no virtual dispatch, no allocations, no `nanoTime()` calls, and the labelling
 * arguments themselves (command name, address, etc.) are never even evaluated.
 *
 * Provide a real implementation to bridge into Micrometer / OpenTelemetry / Prometheus.
 * Bridges override only the entry points they care about; everything else inherits
 * the default empty body or the default `null`-returning observation factory.
 *
 * The contract follows the **observation factory** pattern (mirrors `Timer.start` /
 * `Span.start` in Micrometer and OpenTelemetry): for events with a duration the
 * library asks the recorder to "start an observation"; the recorder is responsible
 * for capturing whatever timing primitive it needs and acting on the matching
 * `completed` / `acquired` / `failed` callback. The library itself never calls
 * `nanoTime()` and never passes a `Duration`.
 */
@ExperimentalReThisMetricsApi
interface MetricsRecorder {
    // ---------- Observation factories (timed events) ----------

    /**
     * Open an observation for a logical command flowing through
     * [eu.vendeli.rethis.topology.TopologyManager.handle], or return `null` to skip.
     * If non-null, exactly one [CommandObservation.completed] will be called.
     *
     * Suggested OTel metrics: `db.client.operation.duration` (histogram),
     * `db.client.operation.count` (counter, attribute `outcome`).
     */
    fun commandStarted(command: String): CommandObservation? = null

    /**
     * Open an observation for a single connection-pool borrow, or `null` to skip.
     * If non-null, exactly one of [AcquireObservation.acquired] /
     * [AcquireObservation.failed] will be called.
     *
     * Suggested OTel metric: `db.client.connection.wait_time` (histogram).
     */
    fun connectionAcquireStarted(address: String): AcquireObservation? = null

    /** Open an observation for a cluster topology refresh, or `null` to skip. */
    fun topologyRefreshStarted(): TopologyRefreshObservation? = null

    // ---------- Plain (non-timed) events ----------

    /** Emitted on every successful release of a connection back to the pool. */
    fun onConnectionReleased(address: String) {}

    /** Emitted whenever the connection factory finishes a creation attempt (success or failure). */
    fun onConnectionCreated(address: String, success: Boolean) {}

    /** Emitted whenever a connection is torn down. The [reason] disambiguates the cause. */
    fun onConnectionDisposed(address: String, reason: DisposeReason) {}

    /**
     * Periodic gauge of pool state. Emitted from the existing pool-observer tick.
     *
     * Suggested OTel metric: `db.client.connection.pool.idle` (gauge).
     */
    fun onPoolSample(address: String, idle: Int) {}

    /**
     * Emitted on every cluster redirect (`MOVED` or `ASK`).
     *
     * Suggested OTel metric: `db.client.cluster.redirect.count` (counter, attribute `kind`).
     */
    fun onClusterRedirect(kind: RedirectKind) {}

    /**
     * Emitted on subscription register (`delta = +1`) and on full removal of all handlers
     * for a target (`delta = -1`).
     */
    fun onSubscriptionChange(targetKind: String, delta: Int) {}
}

/** One-shot observation for a single command. The recorder owns its own timing. */
@ExperimentalReThisMetricsApi
interface CommandObservation {
    /**
     * @param attempts 1 on first-try success, ≥2 if the command was retried.
     * @param outcome  bucketed outcome — see [CommandOutcome].
     */
    fun completed(attempts: Int, outcome: CommandOutcome)
}

/** One-shot observation for a single pool-borrow attempt. */
@ExperimentalReThisMetricsApi
interface AcquireObservation {
    /**
     * @param fastPath `true` when an idle connection was reused, `false` for medium
     * (newly created) and slow (queued) paths.
     */
    fun acquired(fastPath: Boolean)

    fun failed(reason: AcquireFailure)
}

/** One-shot observation for a single cluster topology refresh. */
@ExperimentalReThisMetricsApi
interface TopologyRefreshObservation {
    fun completed(success: Boolean)
}

/**
 * Bucketed outcome of a single command flow through `TopologyManager.handle`.
 * Mapping happens at the call site; redirect exceptions are surfaced as
 * [MetricsRecorder.onClusterRedirect] events instead.
 */
@ExperimentalReThisMetricsApi
enum class CommandOutcome {
    OK,
    ERROR_REDIS,
    ERROR_TIMEOUT,
    ERROR_IO,
    ERROR_OTHER,
}

@ExperimentalReThisMetricsApi
enum class AcquireFailure {
    TIMEOUT,
    OTHER,
}

@ExperimentalReThisMetricsApi
enum class DisposeReason {
    LEFTOVER_BYTES,
    HEALTH_FAIL,
    SHRINK,
    CLOSE,
}

@ExperimentalReThisMetricsApi
enum class RedirectKind {
    MOVED,
    ASK,
}
