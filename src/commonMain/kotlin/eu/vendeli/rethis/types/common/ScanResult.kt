package eu.vendeli.rethis.types.common

data class ScanResult<T>(
    val cursor: String,
    val keys: List<T>,
)
