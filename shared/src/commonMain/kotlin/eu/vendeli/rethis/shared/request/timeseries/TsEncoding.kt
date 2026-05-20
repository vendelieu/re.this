package eu.vendeli.rethis.shared.request.timeseries

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("ENCODING")
sealed class TsEncoding {
    @RedisOption.Token("UNCOMPRESSED")
    data object Uncompressed : TsEncoding()

    @RedisOption.Token("COMPRESSED")
    data object Compressed : TsEncoding()
}
