package eu.vendeli.rethis.shared.request.cluster

sealed class ClusterSyncSlotsOption {
    class Other(vararg val args: String) : ClusterSyncSlotsOption()
}
