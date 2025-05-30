package eu.vendeli.rethis.api.processor.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class KeySpec(
    @SerialName("begin_search") val beginSearch: BeginSearch? = null,
    @SerialName("find_keys") val findKeys: FindKeys? = null,
    @SerialName("RW") val rW: Boolean? = null,
    @SerialName("RO") val rO: Boolean? = null,
    val insert: Boolean? = null,
    val access: Boolean? = null,
    val delete: Boolean? = null
)

@Serializable
internal data class BeginSearch(
    val type: String,
    val spec: BeginSearchSpec? = null
)

@Serializable
internal data class BeginSearchSpec(
    val index: Int? = null
)

@Serializable
internal data class FindKeys(
    val type: String,
    val spec: FindKeysSpec
)

@Serializable
internal data class FindKeysSpec(
    @SerialName("lastkey") val lastKey: Int? = null,
    @SerialName("keystep") val keyStep: Int? = null,
    val limit: Int? = null
)
