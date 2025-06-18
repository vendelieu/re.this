package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readDecimalLong

object IntegerDecoder : ResponseDecoder<Long>(RespCode.INTEGER) {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): Long = input.readDecimalLong()
}
