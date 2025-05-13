package eu.vendeli.rethis.api.processor.type

import kotlinx.serialization.Serializable

@Serializable
internal data class KeySpec(
    val begin_search: BeginSearch? = null,
    val find_keys: FindKeys? = null,
    val RW: Boolean? = null,
    val RO: Boolean? = null,
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
    val lastkey: Int? = null,
    val keystep: Int? = null,
    val limit: Int? = null
)
