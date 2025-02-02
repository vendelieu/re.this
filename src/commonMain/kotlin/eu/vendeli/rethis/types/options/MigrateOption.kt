package eu.vendeli.rethis.types.options

import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

sealed class MigrateOption {
    sealed class Strategy : MigrateOption()
    data object COPY : Strategy()
    data object REPLACE : Strategy()

    sealed class Authorization : MigrateOption()
    class AUTH(
        password: String,
    ) : Authorization(),
        VaryingArgument {
        override val data: List<Argument> = listOf("AUTH".toArgument(), password.toArgument())
    }

    class AUTH2(
        username: String,
        password: String,
    ) : Authorization(),
        VaryingArgument {
        override val data: List<Argument> = listOf("AUTH".toArgument(), username.toArgument(), password.toArgument())
    }

    class KEYS(
        vararg key: String,
    ) : MigrateOption(),
        VaryingArgument {
        override val data: List<Argument> = listOf("KEYS".toArgument(), *key.map { it.toArgument() }.toTypedArray())
    }
}
