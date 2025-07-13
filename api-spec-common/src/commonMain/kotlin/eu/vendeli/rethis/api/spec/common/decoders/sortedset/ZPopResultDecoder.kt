package eu.vendeli.rethis.api.spec.common.decoders.sortedset

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.response.ZPopResult
import eu.vendeli.rethis.api.spec.common.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object ZPopResultDecoder : ResponseDecoder<ZPopResult> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): ZPopResult = ArrayRTypeDecoder.decode(input, charset).let {
        ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap()!!)
    }
}

