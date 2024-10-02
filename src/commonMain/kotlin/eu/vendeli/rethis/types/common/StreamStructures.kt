package eu.vendeli.rethis.types.common

data class XAutoClaimResult(
    val id: String,
    val idle: Long,
    val reclaimed: Long,
    val time: Long,
    val events: List<XRangeEntry>,
)

data class XClaimResult(
    val id: String,
    val idle: Long,
    val time: Long,
    val events: List<XRangeEntry>,
)

data class XInfoConsumer(
    val name: String,
    val pending: Long,
    val idle: Long,
)

data class XInfoGroup(
    val name: String,
    val consumers: Long,
    val pending: Long,
    val lastDeliveredId: String,
)

data class XRangeEntry(
    val id: String,
    val fields: Map<String, String>,
)
