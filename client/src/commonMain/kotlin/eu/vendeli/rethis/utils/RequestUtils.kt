package eu.vendeli.rethis.utils

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.utils.io.core.*
import kotlinx.io.Sink

private fun Sink.writeByteArray(value: ByteArray) {
    append(RespCode.BULK)
    append(value.size.toString())
    appendEOL()
    writeFully(value)
    appendEOL()
}

private fun Sink.append(type: RespCode) {
    writeByte(type.code)
}

private fun Sink.appendEOL() {
    writeFully(EOL)
}
