package eu.vendeli.rethis.serde

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TestData(
    @SerialName("first")
    val field1: String,
    @SerialName("second")
    val field2: Int,
)
