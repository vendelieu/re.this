package eu.vendeli.rethis.api.processor.utils

import com.squareup.kotlinpoet.ClassName
import eu.vendeli.rethis.api.spec.common.types.RespCode

internal val charsetClassName = ClassName("io.ktor.utils.io.charsets", "Charset")

internal val decodersMap: Map<RespCode, Pair<String?, String>> = mapOf(
    RespCode.SIMPLE_STRING to ("SimpleStringDecoder" to "SimpleStringDecoder.decode(input, charset)"),
    RespCode.NULL to (null to "null")
)
