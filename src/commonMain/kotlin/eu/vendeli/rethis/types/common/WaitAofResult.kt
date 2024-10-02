package eu.vendeli.rethis.types.common

data class WaitAofResult(
    val fsyncedRedises: Long,
    val fsyncedReplicas: Long,
)
