package eu.vendeli.rethis.shared.response.common

data class WaitAofResult(
    val fsyncedRedises: Long,
    val fsyncedReplicas: Long,
)
