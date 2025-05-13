package eu.vendeli.rethis.api.spec.common.response

data class WaitAofResult(
    val fsyncedRedises: Long,
    val fsyncedReplicas: Long,
)
