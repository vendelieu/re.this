package eu.vendeli.rethis.shared.response.cluster

import eu.vendeli.rethis.shared.request.cluster.SlotRange
import eu.vendeli.rethis.shared.response.common.HostAndPort

data class ClusterNode(
    val master: HostAndPort,
    val ranges: List<SlotRange>,
    val replicas: List<HostAndPort>,
)
