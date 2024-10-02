package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument

sealed class ScanOption {
    data class Match(
        val pattern: String,
    ) : ScanOption(),
        PairArgument<String, String> {
        override val arg = "MATCH" to pattern
    }

    data class Count(
        val count: Long,
    ) : ScanOption(),
        PairArgument<String, Long> {
        override val arg = "COUNT" to count
    }

    data class Type(
        val type: String,
    ) : ScanOption(),
        PairArgument<String, String> {
        override val arg = "TYPE" to type
    }
}
