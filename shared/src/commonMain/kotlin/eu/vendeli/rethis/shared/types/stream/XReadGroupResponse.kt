package eu.vendeli.rethis.shared.types.stream

import eu.vendeli.rethis.shared.types.RType

data class XReadGroupMessage(
    val id: String,
    val data: Map<String, RType>,
)

data class XReadGroupResponse(
    val stream: String,
    val messages: List<XReadGroupMessage>,
)
