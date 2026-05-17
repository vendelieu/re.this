package eu.vendeli.rethis.shared.request.cluster

sealed class ClusterMigrationOption {
    class Other(vararg val args: String) : ClusterMigrationOption()
}
