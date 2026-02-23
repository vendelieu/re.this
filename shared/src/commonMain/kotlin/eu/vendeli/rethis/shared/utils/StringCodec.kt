package eu.vendeli.rethis.shared.utils

import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

object StringCodec {
    fun decodeToString(charset: Charset, buffer: Buffer): String {
        return buffer.readText(charset)
    }
}
