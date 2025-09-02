package eu.vendeli.rethis.shared.response.common

data class ScanResult<T>(
    val cursor: String,
    val keys: List<T>,
)
