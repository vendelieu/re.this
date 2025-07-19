package eu.vendeli.rethis.api.spec.common.response.common

data class WaitAofResult(
    val fsyncedRedises: Long,
    val fsyncedReplicas: Long,
)
