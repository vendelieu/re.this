package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArg

sealed class MigrateOption {
    sealed class Strategy : MigrateOption()
    data object COPY : Strategy()
    data object REPLACE : Strategy()

    sealed class Authorization : MigrateOption()
    data class AUTH(
        val password: String,
    ) : Authorization(),
        VaryingArgument {
        override val data: List<Argument> = listOf("AUTH".toArg(), password.toArg())
    }

    data class AUTH2(
        val username: String,
        val password: String,
    ) : Authorization(),
        VaryingArgument {
        override val data: List<Argument> = listOf("AUTH".toArg(), username.toArg(), password.toArg())
    }

    class KEYS(
        vararg key: String,
    ) : MigrateOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("KEYS".toArg(), *key.map { it.toArg() }.toTypedArray())
    }
}
