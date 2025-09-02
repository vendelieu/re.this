package eu.vendeli.rethis.shared.request.generic

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class MigrateKey {
    class Actual(val key: String) : MigrateKey()

    @RedisOption.Token("")
    data object Empty : MigrateKey()
}
