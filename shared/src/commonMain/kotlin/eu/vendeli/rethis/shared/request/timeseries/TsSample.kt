package eu.vendeli.rethis.shared.request.timeseries

data class TsSample(
    val key: String,
    val timestamp: String,
    val value: Double,
)
