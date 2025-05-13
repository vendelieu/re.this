package eu.vendeli.rethis.types.response

data class WaitAofResult(
    val fsyncedRedises: Long,
    val fsyncedReplicas: Long,
)
