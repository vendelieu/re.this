package eu.vendeli.rethis.api.spec.common.response

data class ScanResult<T>(
    val cursor: String,
    val keys: List<T>,
)
