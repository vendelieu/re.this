package eu.vendeli.rethis.types.common

enum class ReadFrom {
    MASTER,
    MASTER_PREFERRED,
    REPLICA,
    REPLICA_PREFERRED,
    LOWEST_LATENCY,
    ANY,
    ANY_REPLICA
}
