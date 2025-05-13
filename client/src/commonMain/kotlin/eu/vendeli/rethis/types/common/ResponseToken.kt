package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlinx.io.Source

internal sealed class ResponseToken {
    data class Code(
        val code: RespCode,
        val size: Int? = null,
    ) : ResponseToken()

    data class Data(
        val buffer: Source,
    ) : ResponseToken()
}
