package eu.vendeli.rethis.shared.request.vector

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class VAddOption {
    @RedisOption.Token("REDUCE")
    class Reduce(val dim: Long) : VAddOption()

    @RedisOption.Token("CAS")
    data object Cas : VAddOption()

    sealed class Quantization : VAddOption() {
        @RedisOption.Token("NOQUANT")
        data object NoQuant : Quantization()

        @RedisOption.Token("Q8")
        data object Q8 : Quantization()

        @RedisOption.Token("BIN")
        data object Bin : Quantization()
    }

    @RedisOption.Token("EF")
    class Ef(@RedisOption.Name("build-exploration-factor") val buildExpansion: Long) : VAddOption()

    @RedisOption.Token("SETATTR")
    class SetAttr(val attributes: String) : VAddOption()

    @RedisOption.Token("M")
    class MaxConn(@RedisOption.Name("numlinks") val m: Long) : VAddOption()
}
