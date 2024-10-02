package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.PairArgument
import eu.vendeli.rethis.types.core.TripleArgument

sealed class MigrateOption {
    sealed class Strategy : MigrateOption()
    data object COPY : Strategy()
    data object REPLACE : Strategy()

    sealed class Authorization : MigrateOption()
    data class AUTH(
        val password: String,
    ) : Authorization(),
        PairArgument<String, String> {
        override val arg = "AUTH" to password
    }

    data class AUTH2(
        val username: String,
        val password: String,
    ) : Authorization(),
        TripleArgument<String, String, String> {
        override val arg = Triple("AUTH", username, password)
    }

    class KEYS(
        vararg key: String,
    ) : MigrateOption(),
        PairArgument<String, List<String>> {
        override val arg = "KEYS" to key.toList()
    }
}
