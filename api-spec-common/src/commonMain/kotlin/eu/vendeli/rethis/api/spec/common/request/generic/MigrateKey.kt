package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class MigrateKey {
    class Actual(val key: String) : MigrateKey()

    @RedisOption.Token("")
    data object Empty : MigrateKey()
}
