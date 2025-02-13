package eu.vendeli.rethis.types.response

data class ScanResult<T>(
    val cursor: String,
    val keys: List<T>,
)
