package eu.vendeli.rethis.tests.serde

import kotlinx.serialization.Serializable

@Serializable
data class TestData(
    val field1: String,
    val field2: Int,
)
