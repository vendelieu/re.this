package eu.vendeli.rethis.shared.response.stream

import kotlinx.io.bytestring.ByteString

data class ZMemberBS(
    val member: ByteString,
    val score: Double,
)
