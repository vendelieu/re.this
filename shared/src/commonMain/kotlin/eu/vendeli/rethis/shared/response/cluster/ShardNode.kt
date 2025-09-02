package eu.vendeli.rethis.shared.response.cluster

data class ShardNode(
    val id: String,
    val endpoint: String?,
    val ip: String?,
    val hostname: String?,
    val port: Int?,
    val tlsPort: Int?,
    val role: String,
    val replicationOffset: Long,
    val health: HealthStatus,
) {
    enum class HealthStatus {
        ONLINE, FAILED, LOADING
    }
}
