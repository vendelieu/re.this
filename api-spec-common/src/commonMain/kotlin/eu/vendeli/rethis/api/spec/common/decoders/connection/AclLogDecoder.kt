package eu.vendeli.rethis.api.spec.common.decoders.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object AclLogDecoder : ResponseDecoder<List<String>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): List<String> {
        val code = RespCode.fromCode(input.readByte())
        if (code == RespCode.SIMPLE_STRING && SimpleStringDecoder.decode(input, charset) == "OK") return emptyList()

        val response = ArrayStringDecoder.decode(input, charset, false)

        return response
    }
}
