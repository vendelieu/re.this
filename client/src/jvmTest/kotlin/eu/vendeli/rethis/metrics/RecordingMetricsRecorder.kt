package eu.vendeli.rethis.metrics

import eu.vendeli.rethis.types.interfaces.AcquireFailure
import eu.vendeli.rethis.types.interfaces.AcquireObservation
import eu.vendeli.rethis.types.interfaces.CommandObservation
import eu.vendeli.rethis.types.interfaces.CommandOutcome
import eu.vendeli.rethis.types.interfaces.DisposeReason
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import eu.vendeli.rethis.types.interfaces.MetricsRecorder
import eu.vendeli.rethis.types.interfaces.RedirectKind
import eu.vendeli.rethis.types.interfaces.TopologyRefreshObservation
import java.util.concurrent.CopyOnWriteArrayList

@OptIn(ExperimentalReThisMetricsApi::class)
internal class RecordingMetricsRecorder : MetricsRecorder {
    val events: MutableList<MetricEvent> = CopyOnWriteArrayList()

    sealed class MetricEvent {
        data class CommandStarted(
            val command: String,
        ) : MetricEvent()
        data class CommandCompleted(
            val command: String,
            val attempts: Int,
            val outcome: CommandOutcome,
        ) : MetricEvent()

        data class AcquireStarted(
            val address: String,
        ) : MetricEvent()
        data class Acquired(
            val address: String,
            val fastPath: Boolean,
        ) : MetricEvent()
        data class AcquireFailed(
            val address: String,
            val reason: AcquireFailure,
        ) : MetricEvent()

        data class TopologyRefreshStarted(
            val placeholder: Unit = Unit,
        ) : MetricEvent()
        data class TopologyRefreshCompleted(
            val success: Boolean,
        ) : MetricEvent()

        data class ConnectionReleased(
            val address: String,
        ) : MetricEvent()
        data class ConnectionCreated(
            val address: String,
            val success: Boolean,
        ) : MetricEvent()
        data class ConnectionDisposed(
            val address: String,
            val reason: DisposeReason,
        ) : MetricEvent()
        data class PoolSample(
            val address: String,
            val idle: Int,
        ) : MetricEvent()
        data class ClusterRedirect(
            val kind: RedirectKind,
        ) : MetricEvent()
        data class SubscriptionChange(
            val targetKind: String,
            val delta: Int,
        ) : MetricEvent()
    }

    override fun commandStarted(command: String): CommandObservation {
        events += MetricEvent.CommandStarted(command)
        return object : CommandObservation {
            override fun completed(attempts: Int, outcome: CommandOutcome) {
                events += MetricEvent.CommandCompleted(command, attempts, outcome)
            }
        }
    }

    override fun connectionAcquireStarted(address: String): AcquireObservation {
        events += MetricEvent.AcquireStarted(address)
        return object : AcquireObservation {
            override fun acquired(fastPath: Boolean) {
                events += MetricEvent.Acquired(address, fastPath)
            }

            override fun failed(reason: AcquireFailure) {
                events += MetricEvent.AcquireFailed(address, reason)
            }
        }
    }

    override fun topologyRefreshStarted(): TopologyRefreshObservation {
        events += MetricEvent.TopologyRefreshStarted()
        return object : TopologyRefreshObservation {
            override fun completed(success: Boolean) {
                events += MetricEvent.TopologyRefreshCompleted(success)
            }
        }
    }

    override fun onConnectionReleased(address: String) {
        events += MetricEvent.ConnectionReleased(address)
    }

    override fun onConnectionCreated(address: String, success: Boolean) {
        events += MetricEvent.ConnectionCreated(address, success)
    }

    override fun onConnectionDisposed(address: String, reason: DisposeReason) {
        events += MetricEvent.ConnectionDisposed(address, reason)
    }

    override fun onPoolSample(address: String, idle: Int) {
        events += MetricEvent.PoolSample(address, idle)
    }

    override fun onClusterRedirect(kind: RedirectKind) {
        events += MetricEvent.ClusterRedirect(kind)
    }

    override fun onSubscriptionChange(targetKind: String, delta: Int) {
        events += MetricEvent.SubscriptionChange(targetKind, delta)
    }

    inline fun <reified T : MetricEvent> count(): Int = events.count { it is T }
}
