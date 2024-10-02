package eu.vendeli.rethis.types.options

sealed class XGroupId {
    data class Id(
        val id: String,
    ) : XGroupId()
    data object DOLLAR : XGroupId()
}
