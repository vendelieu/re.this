package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument

sealed class SScanOption {
    data class MATCH(
        val pattern: String,
    ) : SScanOption(),
        PairArgument<String, String> {
        override val arg = "MATCH" to pattern
    }

    data class COUNT(
        val count: Long,
    ) : SScanOption(),
        PairArgument<String, Long> {
        override val arg = "COUNT" to count
    }
}
