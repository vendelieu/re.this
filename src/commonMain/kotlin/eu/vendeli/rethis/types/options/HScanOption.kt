package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument

sealed class HScanOption {
    data class Match(
        val pattern: String,
    ) : HScanOption(),
        PairArgument<String, String> {
        override val arg = "MATCH" to pattern
    }

    data class Count(
        val count: Long,
    ) : HScanOption(),
        PairArgument<String, Long> {
        override val arg = "COUNT" to count
    }

    data object NOVALUES : HScanOption()
}
