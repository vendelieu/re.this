package eu.vendeli.rethis.api.spec.common.response.cluster

import eu.vendeli.rethis.api.spec.common.request.cluster.SlotRange
import eu.vendeli.rethis.api.spec.common.response.common.HostAndPort

data class ClusterNode(
    val master: HostAndPort,
    val ranges: List<SlotRange>,
    val replicas: List<HostAndPort>,
)
