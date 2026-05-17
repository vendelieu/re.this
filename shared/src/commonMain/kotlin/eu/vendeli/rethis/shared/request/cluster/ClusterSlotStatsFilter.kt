package eu.vendeli.rethis.shared.request.cluster

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class ClusterSlotStatsFilter {
    @RedisOption.Token("SLOTSRANGE")
    class SlotsRange(
        @RedisOption.Name("start-slot") val startSlot: Long,
        @RedisOption.Name("end-slot") val endSlot: Long,
    ) : ClusterSlotStatsFilter()

    @RedisOption.Token("ORDERBY")
    class OrderBy(
        val metric: String,
        @RedisOption.Token("LIMIT") val limit: Long? = null,
        val order: Order? = null,
    ) : ClusterSlotStatsFilter() {
        enum class Order { ASC, DESC }
    }
}
